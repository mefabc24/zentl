package org.example.demo3.model.enums;

public enum RowType {
    MELEE (0),
    RANGED (1),
    SIEGE (2),
    ANY (3),
    WEATHER (4); // Oder wir machen NONE

    private final int sortID;

    RowType(int sortID) { this.sortID = sortID; }

    public int getSortID() { return sortID; }
}