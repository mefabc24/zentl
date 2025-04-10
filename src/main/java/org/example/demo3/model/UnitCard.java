package org.example.demo3.model;

import org.example.demo3.model.enums.CardType;
import org.example.demo3.model.enums.RowType;
import org.example.demo3.model.enums.Faction;

public class UnitCard extends BaseCard {
    private final RowType row;

    public UnitCard(String name, int power, String description, Faction faction, RowType row) {
        super(name, power, description, CardType.UNIT, faction);
        this.row = row;
    }

    public RowType getRow() {
        return row;
    }
}