package org.example.demo3.model.enums;

import java.util.List;

public enum Rarity {
    COMMON (0, 100),
    RARE (1, 20),
    EPIC (2, 5),
    LEGENDARY (3, 1),
    MYTHIC (4, 1);

    private final int sortID;
    private final int maxAmount;

    private static final List<Rarity> VALUES = List.of(values());

    Rarity(int sortID, int maxAmount) {
        this.sortID = sortID;
        this.maxAmount = maxAmount;
    }

    public int getSortID() { return sortID; }
    public int getMaxAmount() { return maxAmount; }

    // Search for matching rarity with sortID
    public static Rarity fromSortID(int id) {
        for (Rarity rarity : VALUES) {
            if (rarity.getSortID() == id) {
                return rarity;
            }
        }
        throw new IllegalArgumentException("no rarity found with sortID: " + id);
    }

}
