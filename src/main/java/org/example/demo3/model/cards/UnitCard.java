package org.example.demo3.model.cards;

import org.example.demo3.model.enums.CardType;
import org.example.demo3.model.enums.Fraction;
import org.example.demo3.model.enums.RowType;

public class UnitCard extends BaseCard {
    private final RowType row;

    public UnitCard(String name, int power, String description, Fraction fraction, RowType row) {
        super(name, power, description, CardType.UNIT, fraction);
        this.row = row;
    }

    public RowType getRow() {
        return row;
    }
}