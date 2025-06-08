package org.example.demo3.model.logic;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import org.example.demo3.model.cards.*;
import org.example.demo3.model.enums.Faction;
import org.example.demo3.model.typeadapter.RuntimeTypeAdapterFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.lang.reflect.Type;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import static org.example.demo3.model.constants.Config.*;

public class CardRepository {
    // Singleton-Pattern Implementierung
    private static CardRepository instance;
    private static final Logger logger = LoggerFactory.getLogger(CardRepository.class);

    private List<Card> allCards;
    private static final String resourcePath = JSON_PATH;
    private final Gson gson;

    // Der Konstruktor ist jetzt private, um die Erstellung von außen zu verhindern
    private CardRepository() {
        RuntimeTypeAdapterFactory<Card> adapterFactory = RuntimeTypeAdapterFactory
                .of(Card.class, "cardType")
                .registerSubtype(UnitCard.class, "UNIT")
                .registerSubtype(SpecialCard.class, "SPECIAL")
                .registerSubtype(WeatherCard.class, "WEATHER");

        this.gson = new GsonBuilder()
                .registerTypeAdapterFactory(adapterFactory)
                .setPrettyPrinting()
                .create();

        this.allCards = load();

        if (this.allCards == null) {
            this.allCards = new ArrayList<>();
            logger.warn("allCards war null nach dem Laden, wird mit leerer Liste initialisiert.");
        }
    }

    // Öffentliche statische Methode, um die einzige Instanz zu erhalten
    public static synchronized CardRepository getInstance() {
        if (instance == null) {
            instance = new CardRepository();
        }
        return instance;
    }


    // Getter
    public List<Card> getAllCards() { return allCards; }

    public List<Card> getUnlockableCards() {
        return allCards.stream()
                .filter(card -> card.getAmount() < card.getMaxAmount())
                .toList();
    }

    public int getLastID() {
        return allCards.stream()
                .mapToInt(Card::getId)
                .max()
                .orElse(-1);
    }

    // CardInstances für Deckbuilder
    public List<CardInstance> getDisplayableCardInstances(Faction faction) {
        if (allCards == null) {
            return Collections.emptyList();
        }
        return allCards.stream()
                .filter(card -> card.getFaction() == faction)
                .flatMap(card -> java.util.stream.IntStream.range(0, card.getAmount())
                        .mapToObj(i -> new CardInstance(card)))
                .toList();
    }

    // Deck-Lade-Methoden
    public List<Card> getSavedDeck(Faction faction) {
        if (allCards == null || allCards.isEmpty()) {
            return Collections.emptyList();
        }

        List<Card> savedDeck = allCards.stream()
                .filter(card -> card.getFaction() == faction && card.getSelectedAmount() > 0 && card.getAmount() > 0)
                .flatMap(card -> Collections.nCopies(card.getSelectedAmount(), card).stream())
                .toList();

        if (savedDeck.size() >= MIN_SELECTION) {
            logger.info("Gespeichertes Deck für Fraktion {} geladen. Größe: {}", faction, savedDeck.size());
            return new ArrayList<>(savedDeck);
        }

        logger.info("Kein gültiges gespeichertes Deck für Fraktion {} gefunden (nur {}/{} Karten).", faction, savedDeck.size(), MIN_SELECTION);
        return Collections.emptyList();
    }

    public List<Card> getRandomDeck(Faction faction) {
        if (allCards == null || allCards.isEmpty()) {
            return Collections.emptyList();
        }

        List<Card> potentialCards = allCards.stream()
                .filter(card -> card.getFaction() == faction && card.getAmount() > 0)
                .toList();

        if (potentialCards.isEmpty()) {
            logger.warn("Keine freigeschalteten Karten für Fraktion {} verfügbar, um ein Zufallsdeck zu erstellen.", faction);
            return Collections.emptyList();
        }

        List<Card> randomDeck = new ArrayList<>();
        Random random = new Random();

        List<Card> candidates = new ArrayList<>(potentialCards);

        while (randomDeck.size() < RANDOMIZER_CARD_AMOUNT && !candidates.isEmpty()) {
            Card candidate = candidates.get(random.nextInt(candidates.size()));
            long countInDeck = randomDeck.stream().filter(c -> c.getId() == candidate.getId()).count();

            if (countInDeck < candidate.getMaxAmount()) {
                randomDeck.add(candidate);
            } else {
                candidates.removeIf(c -> c.getId() == candidate.getId());
            }
        }

        logger.info("Zufälliges Deck für Fraktion {} erstellt. Größe: {}", faction, randomDeck.size());
        return randomDeck;
    }

    // Save & Load
    public void save() { saveCardsToJson(); }

    public List<Card> load() {
        this.allCards = loadCardsFromJson();
        if (this.allCards.isEmpty()) {
            this.allCards = new ArrayList<>();
        }
        return this.allCards;
    }

    // (De-)Serialization
    private List<Card> loadCardsFromJson() {
        logger.info("Versuche Karten zu laden von Ressourcenpfad: {}", resourcePath);
        try (InputStream is = getClass().getResourceAsStream(resourcePath)) {
            if (is == null) {
                logger.error("Ressourcendatei nicht gefunden: {}", resourcePath);
                return new ArrayList<>();
            }

            try (Reader reader = new InputStreamReader(is, StandardCharsets.UTF_8)) {
                Type cardListType = new TypeToken<ArrayList<Card>>(){}.getType();
                List<Card> loaded = this.gson.fromJson(reader, cardListType);

                if (loaded == null) {
                    logger.warn("Gson.fromJson hat null zurückgegeben. JSON-Datei ist möglicherweise leer oder fehlerhaft formatiert.");
                    return new ArrayList<>();
                }

                loaded.forEach(card -> {
                    if (card instanceof BaseCard bc) {
                        bc.initMaxAmount();
                    }
                });

                logger.info("{} Karten erfolgreich aus JSON deserialisiert.", loaded.size());
                return loaded;

            } catch (JsonSyntaxException e) {
                logger.error("JSON Syntax Fehler beim Lesen von {}: {}", resourcePath, e.getMessage(), e);
                return new ArrayList<>();
            }
        } catch (IOException e) {
            logger.error("IO Fehler beim Lesen von {}: {}", resourcePath, e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    private void saveCardsToJson() {
        URL resourceUrl = getClass().getResource(resourcePath);
        if (resourceUrl == null) {
            logger.error("Ressource zum Speichern nicht gefunden: {}", resourcePath);
            return;
        }

        try {
            Path outputPath = Paths.get(resourceUrl.toURI());
            try (BufferedWriter writer = Files.newBufferedWriter(outputPath, StandardCharsets.UTF_8)) {
                this.gson.toJson(allCards, writer);
                logger.info("Karten erfolgreich gespeichert in {}", outputPath.toAbsolutePath());
            } catch (IOException e) {
                logger.error("Fehler beim Speichern der Karten nach {}: {}", outputPath.toAbsolutePath(), e.getMessage(), e);
            }
        } catch (URISyntaxException e) {
            logger.error("Fehler beim Speichern der Karten: {}", e.getMessage(), e);
        }
    }
}