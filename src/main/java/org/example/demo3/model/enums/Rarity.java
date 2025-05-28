package org.example.demo3.model.enums;

public enum Rarity {
    COMMON (0),
    RARE (1),
    EPIC (2),
    LEGENDARY (3),
    MYTHIC (4),
    WEATHER (5),;

    private final int sortID;

    Rarity(int sortID) { this.sortID = sortID; }

    public int getSortID() { return sortID; }
}
