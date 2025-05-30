package org.example.demo3.model.logic;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import org.example.demo3.model.cards.*;
import org.example.demo3.model.enums.CardType;
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
import java.util.List;
import java.util.stream.Collectors;

import static org.example.demo3.model.constants.Config.*;

public class CardRepository {
    private List<Card> allCards;
    private static final String resourcePath = JSON_PATH;
    private final Gson gson;

    public CardRepository() {
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

    // Getter
    public List<Card> getAllCards() { return allCards; }

    public List<Card> getSelectedCards() {
        return allCards.stream()
                .filter(card -> card.getSelectedAmount() > 0)
                .collect(Collectors.toList());
    }

    public List<Card> getFactionCards(Faction faction) {
        return allCards.stream()
                .filter(card -> card.getFaction() == faction)
                .collect(Collectors.toList());
    }

    public List<Card> getSelectedFactionCards(Faction faction) {
        return allCards.stream()
                .filter(card -> card.getSelectedAmount() > 0)
                .filter(card -> card.getFaction() == faction)
                .collect(Collectors.toList());
    }

    public List<Card> getCardsByType(CardType cardType) {
        return allCards.stream()
                .filter(card -> card.getCardType() == cardType)
                .collect(Collectors.toList());
    }

    public List<Card> getSelectedCardsByType(CardType cardType) {
        return allCards.stream()
                .filter(card -> card.getSelectedAmount() > 0)
                .filter(card -> card.getCardType() == cardType)
                .collect(Collectors.toList());
    }

    public int getLastID() {
        return allCards.stream()
                .mapToInt(Card::getId)
                .max()
                .orElse(-1);
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

    // Add & Remove
    public void addCard(Card card) { allCards.add(card); }
    public void removeCard(Card card) { allCards.remove(card); }

    // (De-)Serialization
    private List<Card> loadCardsFromJson() {
        System.out.println(this.resourcePath);
        System.out.println("Versuche Karten zu laden von Ressourcenpfad: " + this.resourcePath);
        InputStream is = getClass().getResourceAsStream(this.resourcePath);

        if (is == null) {
            if (!this.resourcePath.startsWith("/")) {
                is = getClass().getResourceAsStream("/" + this.resourcePath);
            }
            if (is == null) {
                System.err.println("Ressourcendatei nicht gefunden: " + this.resourcePath + " (auch nicht mit führendem Slash)");
                return new ArrayList<>();
            }
        }

        URL resourceUrl = getClass().getResource(this.resourcePath.startsWith("/") ? this.resourcePath : "/" + this.resourcePath);
        if (resourceUrl == null) resourceUrl = getClass().getResource(this.resourcePath);
        System.out.println("Ressourcendatei gefunden unter: " + (resourceUrl != null ? resourceUrl.toString() : "Konnte URL nicht auflösen"));


        try (Reader reader = new InputStreamReader(is, StandardCharsets.UTF_8)) {

            Type cardListType = new TypeToken<ArrayList<Card>>(){}.getType();
            List<Card> loaded = this.gson.fromJson(reader, cardListType);

            if (loaded == null) {
                System.err.println("Gson.fromJson hat null zurückgegeben. JSON-Datei ist möglicherweise leer oder fehlerhaft formatiert.");
                return new ArrayList<>();
            }
            System.out.println(loaded.size() + " Karten erfolgreich aus JSON deserialisiert.");

            for(Card c : loaded) {
                if (c == null) {
                    System.err.println("WARNUNG: Ein null-Kartenobjekt wurde aus JSON geladen. Überprüfe die JSON-Datei und die Adapterkonfiguration.");
                    continue;
                }

                if (c instanceof BaseCard bc) {
                    bc.initMaxAmount();
                }
                System.out.println("Geladene Karte: ID=" + c.getId() + ", Name='" + c.getName() + "' ist vom Typ: " + c.getClass().getSimpleName() + " mit CardType Enum: " + c.getCardType() + " DEBUG: " + c.getMaxAmount());
            }
            return loaded;
        } catch (JsonSyntaxException e) {
            System.err.println("JSON Syntax Fehler beim Lesen von " + this.resourcePath + ": " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        } catch (IOException e) {
            System.err.println("IO Fehler beim Lesen von " + this.resourcePath + ": " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        } catch (Exception e) {
            System.err.println("Allgemeiner Fehler beim Laden/Deserialisieren von Karten: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    private void saveCardsToJson() {
        URL resourceUrl = getClass().getResource(this.resourcePath);

        if (resourceUrl == null) {
            if (!this.resourcePath.startsWith("/")) {
                resourceUrl = getClass().getResource("/" + this.resourcePath);
            }
            if (resourceUrl == null) {
                System.err.println("Ressource zum Speichern nicht gefunden: " + this.resourcePath);
                System.err.println("Versuche, in eine Datei 'cards_fallback.json' im Arbeitsverzeichnis zu speichern.");
                try (Writer writer = new FileWriter("cards_fallback.json", StandardCharsets.UTF_8)) {
                    this.gson.toJson(allCards, writer);
                    System.out.println("Fallback-Speichern erfolgreich in cards_fallback.json");
                } catch (IOException e) {
                    System.err.println("Fehler beim Fallback-Speichern: " + e.getMessage());
                    e.printStackTrace();
                }
                return;
            }
        }

        System.out.println("URL zur Ressource für Speichern ermittelt: " + resourceUrl.toString());

        try {
            Path outputPath;
            if ("file".equals(resourceUrl.getProtocol())) {
                outputPath = Paths.get(resourceUrl.toURI());
                System.out.println("DEBUG: Absoluter Zieldateipfad für Speichern ist: " + outputPath.toAbsolutePath().toString());
            } else {
                System.err.println("Kann nicht direkt in Ressource mit Protokoll '" + resourceUrl.getProtocol() + "' schreiben. Dies ist typisch, wenn die App als JAR läuft.");
                System.err.println("Versuche, in eine Datei 'cards_protocol_fallback.json' im Arbeitsverzeichnis zu speichern.");
                try (Writer writer = new FileWriter("cards_protocol_fallback.json", StandardCharsets.UTF_8)) {
                    this.gson.toJson(allCards, writer);
                    System.out.println("Protokoll-Fallback-Speichern erfolgreich in cards_protocol_fallback.json");
                } catch (IOException e) {
                    System.err.println("Fehler beim Protokoll-Fallback-Speichern: " + e.getMessage());
                    e.printStackTrace();
                }
                return;
            }

            try (BufferedWriter writer = Files.newBufferedWriter(outputPath, StandardCharsets.UTF_8)) {
                this.gson.toJson(allCards, writer);
                System.out.println("Karten erfolgreich gespeichert in " + outputPath.toAbsolutePath());
                System.out.println("HINWEIS: Wenn dies eine Ressourcendatei im Build-Verzeichnis ist (z.B. target/classes),");
                System.out.println("wird die Originaldatei in src/main/resources NICHT automatisch aktualisiert.");
            } catch (IOException e) {
                System.err.println("Fehler beim Speichern der Karten nach " + outputPath.toAbsolutePath() + ": " + e.getMessage());
                e.printStackTrace();
            }
        } catch (URISyntaxException e) {
            System.err.println("Fehler beim Umwandeln der Ressourcen-URL in URI: " + e.getMessage());
            e.printStackTrace();
        }  catch (Exception e) {
            System.err.println("Allgemeiner Fehler beim Speichern von Karten: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
