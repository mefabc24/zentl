package org.example.demo3.model;

import org.example.demo3.model.enums.CardType;
import org.example.demo3.model.enums.Faction;

public abstract class BaseCard implements Card {
    protected final String name;
    protected final int power;
    protected final String description;
    protected final CardType type;
    protected final Faction faction;

    public BaseCard(String name, int power, String description, CardType type, Faction faction) {
        this.name = name;
        this.power = power;
        this.description = description;
        this.type = type;
        this.faction = faction;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public int getPower() {
        return power;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public CardType getType() {
        return type;
    }

    public Faction getFaction() {
        return faction;
    }
}