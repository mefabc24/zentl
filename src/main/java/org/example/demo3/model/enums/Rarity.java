package org.example.demo3.model.enums;

public enum Rarity {
    COMMON (0, 100),
    RARE (1, 20),
    EPIC (2, 5),
    LEGENDARY (3, 1),
    MYTHIC (4, 1),
    WEATHER (5, 20),;

    private final int sortID;
    private final int maxAmount;

    Rarity(int sortID, int maxAmount) {
        this.sortID = sortID;
        this.maxAmount = maxAmount;
    }

    public int getSortID() { return sortID; }

    public int getMaxAmount() { return maxAmount; }
}
