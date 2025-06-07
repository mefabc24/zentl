package org.example.demo3.model.logic;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import org.example.demo3.model.cards.*;
import org.example.demo3.model.enums.Faction;
import org.example.demo3.model.typeadapter.RuntimeTypeAdapterFactory;

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
import java.util.stream.Collectors;

import static org.example.demo3.model.constants.Config.*;

public class CardRepository {
    // Singleton-Pattern Implementierung
    private static CardRepository instance;

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
            System.err.println("CardRepository: allCards war null nach dem Laden, initialisiere mit leerer Liste.");
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

    public int getLastID() {
        return allCards.stream()
                .mapToInt(Card::getId)
                .max()
                .orElse(-1);
    }

    // Deck-Lade-Methoden
    public List<Card> getSavedDeck(Faction faction) {
        if (allCards == null || allCards.isEmpty()) {
            return Collections.emptyList();
        }

        List<Card> savedDeck = allCards.stream()
                .filter(card -> card.getFaction() == faction && card.getSelectedAmount() > 0 && card.isUnlocked())
                .flatMap(card -> Collections.nCopies(card.getSelectedAmount(), card).stream())
                .collect(Collectors.toList());

        if (savedDeck.size() >= MIN_SELECTION) {
            System.out.println("Gespeichertes Deck für Fraktion " + faction + " geladen. Größe: " + savedDeck.size());
            return new ArrayList<>(savedDeck);
        }

        System.out.println("Kein gültiges gespeichertes Deck für Fraktion " + faction + " gefunden (nur " + savedDeck.size() + "/" + MIN_SELECTION + " Karten).");
        return Collections.emptyList();
    }

    public List<Card> getRandomDeck(Faction faction) {
        if (allCards == null || allCards.isEmpty()) {
            return Collections.emptyList();
        }

        List<Card> potentialCards = allCards.stream()
                .filter(card -> card.getFaction() == faction && card.isUnlocked() && card.getAmount() > 0)
                .collect(Collectors.toList());

        if (potentialCards.isEmpty()) {
            System.err.println("Keine freigeschalteten Karten für Fraktion " + faction + " verfügbar, um ein Zufallsdeck zu erstellen.");
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

        System.out.println("Zufälliges Deck für Fraktion " + faction + " erstellt. Größe: " + randomDeck.size());
        return randomDeck;
    }

    // Save & Load
    public void save() { saveCardsToJson(); }

    public List<Card> load() {
        this.allCards = loadCardsFromJson();
        if (this.allCards == null) {
            this.allCards = new ArrayList<>();
        }
        return this.allCards;
    }

    // (De-)Serialization
    private List<Card> loadCardsFromJson() {
        System.out.println("Versuche Karten zu laden von Ressourcenpfad: " + resourcePath);
        try (InputStream is = getClass().getResourceAsStream(resourcePath)) {
            if (is == null) {
                System.err.println("Ressourcendatei nicht gefunden: " + resourcePath);
                return new ArrayList<>();
            }

            try (Reader reader = new InputStreamReader(is, StandardCharsets.UTF_8)) {
                Type cardListType = new TypeToken<ArrayList<Card>>(){}.getType();
                List<Card> loaded = this.gson.fromJson(reader, cardListType);

                if (loaded == null) {
                    System.err.println("Gson.fromJson hat null zurückgegeben. JSON-Datei ist möglicherweise leer oder fehlerhaft formatiert.");
                    return new ArrayList<>();
                }

                loaded.forEach(card -> {
                    if (card instanceof BaseCard bc) {
                        bc.initMaxAmount();
                    }
                });

                System.out.println(loaded.size() + " Karten erfolgreich aus JSON deserialisiert.");
                return loaded;

            } catch (JsonSyntaxException e) {
                System.err.println("JSON Syntax Fehler beim Lesen von " + resourcePath + ": " + e.getMessage());
                e.printStackTrace();
                return new ArrayList<>();
            }
        } catch (IOException e) {
            System.err.println("IO Fehler beim Lesen von " + resourcePath + ": " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    private void saveCardsToJson() {
        URL resourceUrl = getClass().getResource(resourcePath);
        if (resourceUrl == null) {
            System.err.println("Ressource zum Speichern nicht gefunden: " + resourcePath);
            return;
        }

        try {
            Path outputPath = Paths.get(resourceUrl.toURI());
            try (BufferedWriter writer = Files.newBufferedWriter(outputPath, StandardCharsets.UTF_8)) {
                this.gson.toJson(allCards, writer);
                System.out.println("Karten erfolgreich gespeichert in " + outputPath.toAbsolutePath());
            } catch (IOException e) {
                System.err.println("Fehler beim Speichern der Karten nach " + outputPath.toAbsolutePath() + ": " + e.getMessage());
                e.printStackTrace();
            }
        } catch (URISyntaxException e) {
            System.err.println("Fehler beim Speichern der Karten: " + e.getMessage());
            e.printStackTrace();
        }
    }
}