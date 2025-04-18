package org.example.demo3.model.cards;

import org.example.demo3.model.enums.CardType;
import org.example.demo3.model.enums.Fraction;

public class SpecialCard extends BaseCard {
    public SpecialCard(String name, String description, Fraction fraction) {
        super(name, 0, description, CardType.SPECIAL, fraction);
    }
}