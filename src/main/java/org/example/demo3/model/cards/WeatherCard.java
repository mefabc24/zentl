package org.example.demo3.model.cards;

import org.example.demo3.model.enums.CardType;
import org.example.demo3.model.enums.Faction;
import org.example.demo3.model.enums.Rarity;
import org.example.demo3.model.enums.RowType;

public class WeatherCard extends BaseCard {
    public WeatherCard() {
        super();
        this.cardType = CardType.WEATHER;
    }

    public WeatherCard(int id, int power, String name, String description, Faction faction, RowType rowType,
                       Rarity rarity, String imagePath, boolean isUnlocked) {
        super(id, power, name, description, faction, rowType, CardType.WEATHER, rarity, imagePath, isUnlocked);
    }
}

