package org.example.demo3.model.cards;

import org.example.demo3.model.enums.*;

public class UnitCard extends BaseCard {
    public UnitCard() {
        super();
        this.cardType = CardType.UNIT;
    }

    public UnitCard(int id, int power, int amount, String name, String description, Faction faction, RowType rowType,
                    Rarity rarity, String imagePath, boolean isUnlocked) {
        super(id, power, amount, name, description, faction, rowType, CardType.UNIT, rarity, imagePath, isUnlocked, null);
    }
}