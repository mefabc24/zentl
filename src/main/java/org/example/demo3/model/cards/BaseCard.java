package org.example.demo3.model.cards;

import org.example.demo3.model.enums.CardType;
import org.example.demo3.model.enums.Fraction;

public abstract class BaseCard implements Card {
    protected final String name;
    protected final int power;
    protected final String description;
    protected final CardType type;
    protected final Fraction fraction;

    public BaseCard(String name, int power, String description, CardType type, Fraction fraction) {
        this.name = name;
        this.power = power;
        this.description = description;
        this.type = type;
        this.fraction = fraction;
    }

    public String getName() {
        return name;
    }

    public int getPower() {
        return power;
    }

    public String getDescription() {
        return description;
    }

    public CardType getType() {
        return type;
    }

    public Fraction getfraction() {
        return fraction;
    }
}