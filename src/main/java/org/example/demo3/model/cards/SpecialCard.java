package org.example.demo3.model.cards;

import org.example.demo3.model.enums.CardType;
import org.example.demo3.model.enums.Fraction;
import org.example.demo3.model.enums.RowType;

public class SpecialCard extends BaseCard {
    public SpecialCard(String name, String description, Fraction fraction, RowType rowType) {
        super(name, 0, description, CardType.SPECIAL, fraction, rowType);
    }
}