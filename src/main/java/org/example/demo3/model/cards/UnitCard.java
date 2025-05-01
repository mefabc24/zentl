package org.example.demo3.model.cards;

import org.example.demo3.model.enums.CardType;
import org.example.demo3.model.enums.Fraction;
import org.example.demo3.model.enums.RowType;

public class UnitCard extends BaseCard {

    public UnitCard(String name, int power, String description, Fraction fraction, RowType row) {
        super(name, power, description, CardType.UNIT, fraction, row);
    }

}