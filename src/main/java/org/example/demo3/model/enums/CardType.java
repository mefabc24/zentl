package org.example.demo3.model.enums;

public enum CardType {
    UNIT (0),
    SPECIAL (1),
    WEATHER (2);

    private final int sortID;

    CardType(int sortID) { this.sortID = sortID; }

    public int getSortID() { return sortID; }
}