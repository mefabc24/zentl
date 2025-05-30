package org.example.demo3.model.cards;

import javafx.scene.image.Image;
import org.example.demo3.model.enums.CardType;
import org.example.demo3.model.enums.Faction;
import org.example.demo3.model.enums.Rarity;
import org.example.demo3.model.enums.RowType;

import java.io.InputStream;


public abstract class BaseCard implements Card {
    // Console Colors
    private static final String RESET = "\u001B[0m";
    private static final String RED = "\u001B[31m";
    private static final String GREEN = "\u001B[32m";


    protected int id;
    protected int power;
    protected int amount;
    protected int selectedAmount;
    protected int maxAmount;

    protected String name;
    protected String description;
    protected String imagePath;

    protected Faction faction;
    protected RowType rowType;
    protected CardType cardType;
    protected Rarity rarity;

    protected transient Image cardImage;
    protected boolean isUnlocked;

    protected BaseCard() { this.selectedAmount = 0; }

    protected BaseCard(int id, int power, int amount, String name, String description,
                       Faction faction, RowType rowType, CardType cardType, Rarity rarity, String imagePath, boolean isUnlocked) {
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
        this.isUnlocked = isUnlocked;

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
    @Override public boolean isUnlocked() { return isUnlocked; }

    // Setter
    public void setId(int id) { this.id = id; }
    public void setPower(int power) { this.power = power; }
    public void setAmount(int amount) { this.amount = amount; }
    public void setMaxAmount(int maxAmount) { this.maxAmount = maxAmount; }
    public void setName(String name) { this.name = name; }
    public void setDescription(String description) { this.description = description; }
    public void setFaction(Faction faction) { this.faction = faction; }
    public void setRowType(RowType rowType) { this.rowType = rowType; }
    public void setCardType(CardType cardType) { this.cardType = cardType; }
    public void setRarity(Rarity rarity) { this.rarity = rarity; }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
        this.cardImage = null; // Bild zurücksetzen, damit es neu geladen wird
    }

    @Override public void setSelectedAmount(int selectedAmount) { this.selectedAmount = selectedAmount; }
    @Override public void setUnlocked(boolean unlocked) { this.isUnlocked = unlocked; }

    public void initMaxAmount() {
        if (this.rarity != null) {
            this.maxAmount = this.rarity.getMaxAmount();
        }
    }

    // Bild bei Bedarf laden (Lazy Loading)
    @Override
    public Image getImage() {
        if (this.cardImage == null && this.imagePath != null && !this.imagePath.isEmpty()) {
            try {
                InputStream stream = getClass().getResourceAsStream(this.imagePath);
                if (stream != null) {
                    this.cardImage = new Image(stream);
                    if (this.cardImage.isError()) {
                        System.err.println("Fehler beim Erstellen des Image-Objekts für: " + this.imagePath + " für Karte " + this.name + " - Exception: " + this.cardImage.getException());
                        this.cardImage = null;
                    }
                } else {
                    System.err.println("Bildressource nicht gefunden: " + this.imagePath + " für Karte " + this.name);
                }
            } catch (Exception e) {
                System.err.println("Fehler beim Laden des Bildes '" + this.imagePath + "' für Karte " + this.name + ": " + e.getMessage());
                e.printStackTrace();
            }
        }
        return this.cardImage;
    }

    @Override
    public String toString() {
        String statusColor = this.isUnlocked ? GREEN : RED;

        return statusColor + "{" + id + "} [" + faction + "] " + name + " " + power + " -" + rarity + "- (" + rowType + ")  unlocked/selAmount" + isUnlocked + "/" + "DEBUG maxAmount: " + maxAmount + selectedAmount + RESET;
    }
}