package org.example.demo3.model.cards;

import com.google.gson.annotations.Expose;
import javafx.scene.image.Image;
import org.example.demo3.model.enums.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Objects;

import java.io.InputStream;

@SuppressWarnings("unused")
public abstract class BaseCard implements Card {
    // Console Colors
    private static final String RESET = "\u001B[0m";
    private static final String RED = "\u001B[31m";
    private static final String GREEN = "\u001B[32m";

    private static final Logger logger = LoggerFactory.getLogger(BaseCard.class);

    @Expose protected int id;
    @Expose protected int power;
    @Expose protected int amount;
    @Expose protected int selectedAmount;
    @Expose protected int maxAmount;

    @Expose protected String name;
    @Expose protected String description;
    @Expose protected String imagePath;

    @Expose protected Faction faction;
    @Expose protected RowType rowType;
    protected CardType cardType;
    @Expose protected Rarity rarity;
    @Expose protected EffectType effectType;

    protected transient Image cardImage;

    protected BaseCard() {
        this.selectedAmount = 0;
        this.effectType = EffectType.NONE;
    }

    // Dieser Konstruktor dient eher als Referenz, Gson nutzt den parameterlosen
    protected BaseCard(int id, int power, int amount, String name, String description,
                       Faction faction, RowType rowType, CardType cardType, Rarity rarity,
                       String imagePath, EffectType effectType) {
        this.id = id;
        this.power = power;
        this.amount = amount;
        this.name = name;
        this.description = description;
        this.faction = faction;
        this.rowType = rowType;
        this.cardType = cardType;
        this.rarity = rarity;
        this.imagePath = imagePath;
        this.effectType = (effectType != null) ? effectType : EffectType.NONE;

        this.selectedAmount = 0;
        this.maxAmount = this.rarity.getMaxAmount();
    }

    // Getter
    @Override public int getId() { return id; }
    @Override public int getPower() { return power; }
    @Override public int getAmount() { return amount; }
    @Override public int getSelectedAmount() { return selectedAmount; }
    @Override public int getMaxAmount() { return maxAmount; }
    @Override public String getName() { return name; }
    @Override public String getDescription() { return description; }
    @Override public String getImagePath() { return imagePath; }
    @Override public Faction getFaction() { return faction; }
    @Override public RowType getRowType() { return rowType; }
    @Override public CardType getCardType() { return cardType; }
    @Override public Rarity getRarity() { return rarity; }
    @Override public EffectType getEffectType() { return effectType; } // Hinzugefügt

    // Setter
    public void setId(int id) { this.id = id; }
    public void setPower(int power) { this.power = power; }

    public void setMaxAmount(int maxAmount) { this.maxAmount = maxAmount; }
    public void setName(String name) { this.name = name; }
    public void setDescription(String description) { this.description = description; }
    public void setFaction(Faction faction) { this.faction = faction; }
    public void setRowType(RowType rowType) { this.rowType = rowType; }
    public void setCardType(CardType cardType) { this.cardType = cardType; }
    public void setEffectType(EffectType effectType) { this.effectType = effectType; } // Hinzugefügt

    public void setRarity(Rarity rarity) {
        this.rarity = rarity;
        if (rarity != null) {
            this.maxAmount = rarity.getMaxAmount();
        }
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
        this.cardImage = null;
    }

    @Override public void setSelectedAmount(int selectedAmount) { this.selectedAmount = selectedAmount; }
    @Override public void setAmount(int amount) { this.amount = amount; }

    public void initMaxAmount() {
        if (this.rarity != null) {
            this.maxAmount = this.rarity.getMaxAmount();
        }
    }

    @Override
    public Image getImage() {
        if (this.cardImage == null && this.imagePath != null && !this.imagePath.isEmpty()) {


            String absolutePath = this.imagePath;
            if (!absolutePath.startsWith("/")) {
                absolutePath = "/" + absolutePath;
            }

            try (InputStream stream = getClass().getResourceAsStream(absolutePath)) {
                if (stream != null) {
                    this.cardImage = new Image(stream);
                    if (this.cardImage.isError()) {
                        System.err.println("Fehler beim Erstellen des Image-Objekts für: " + absolutePath + " für Karte " + this.name + " - Exception: " + this.cardImage.getException());
                        this.cardImage = null;
                    }
                } else {
                    System.err.println("Bild nicht gefunden: " + absolutePath + " für Karte " + this.name);
                }
            } catch (Exception e) {
                logger.error("Fehler beim Laden des Bildes '{}' für Karte '{}'", absolutePath, this.name, e);
            }
        }
        return this.cardImage;
    }

    @Override
    public String toString() {
        String color = this.getAmount() > 0 ? GREEN : RED;
        return color + "{" + id + "} [" + faction + "] " + name + " " + power + " -" + rarity + "- (" + rowType + ") amount=" + amount + " effect=" + effectType + RESET;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Card otherCard)) return false;
        return this.id == otherCard.getId();
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}