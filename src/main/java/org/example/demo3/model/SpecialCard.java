package org.example.demo3.model;

import org.example.demo3.model.enums.CardType;
import org.example.demo3.model.enums.Faction;

public class SpecialCard extends BaseCard {
    public SpecialCard(String name, String description, Faction faction) {
        super(name, 0, description, CardType.SPECIAL, faction);
    }
}