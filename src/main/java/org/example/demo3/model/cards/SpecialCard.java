package org.example.demo3.model.cards;

import org.example.demo3.model.enums.*;

public class SpecialCard extends BaseCard {
    public SpecialCard() {
        super();
        this.cardType = CardType.SPECIAL;
    }

    public SpecialCard(int id, int power, int amount, String name, String description, Faction faction, RowType rowType,
                       Rarity rarity, String imagePath, boolean isUnlocked) {
        super(id, power, amount, name, description, faction, rowType, CardType.SPECIAL, rarity, imagePath, isUnlocked, null);
    }
}