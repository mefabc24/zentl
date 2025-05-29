package org.example.demo3.model.cards;

import org.example.demo3.model.enums.*;

public class WeatherCard extends BaseCard {

    private WeatherType weatherType;

    public WeatherCard() {
        super();
        this.cardType = CardType.WEATHER;
    }

    public WeatherCard(int id, int power, int amount, String name, String description, Faction faction, RowType rowType,
                       Rarity rarity, String imagePath, boolean isUnlocked, WeatherType weatherType) {
        super(id, power, amount, name, description, faction, rowType, CardType.WEATHER, rarity, imagePath, isUnlocked);

        this.weatherType = weatherType;
    }
}

