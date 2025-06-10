package org.example.demo3.model.enums;

import java.util.List;

public enum Rarity {
    COMMON (0, 100), // weight entfernt
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

    // Getter
    public int getSortID() { return sortID; }
    public int getMaxAmount() { return maxAmount; }

    // Sucht passende Rarity anhand der SortID
    public static Rarity fromSortID(int id) {
        for (Rarity rarity : VALUES) { // VALUES ist die Liste, die du schon hast
            if (rarity.getSortID() == id) {
                return rarity;
            }
        }
        // Dieser Fehlerfall ist wichtig, um Probleme im Code schnell zu finden.
        throw new IllegalArgumentException("Keine Rarity mit der sortID gefunden: " + id);
    }

}
