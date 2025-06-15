package org.example.demo3.model.cards;

import org.example.demo3.model.enums.*;

public class WeatherCard extends BaseCard {

    private WeatherType weatherType;

    public WeatherCard() {
        super();
        this.cardType = CardType.WEATHER;
    }

    public WeatherCard(int id, int power, int amount, String name, String description, Faction faction, RowType rowType,
                       Rarity rarity, String imagePath, WeatherType weatherType) {
        super(id, power, amount, name, description, faction, rowType, CardType.WEATHER, rarity, imagePath, determineEffectType(weatherType));

        this.weatherType = weatherType;
    }

    // helper method to keep super Constructor clean
    public static EffectType determineEffectType(WeatherType weatherType) {
        if (weatherType == null) {
            return EffectType.NONE;
        }
        return switch (weatherType) {
            case FROST -> EffectType.WEATHER_FROST;
            case FOG -> EffectType.WEATHER_FOG;
            case RAIN -> EffectType.WEATHER_RAIN;
        };
    }

    // Get-Set
    public WeatherType getWeatherType() {
        return weatherType;
    }

    public void setWeatherType(WeatherType weatherType) {
        this.weatherType = weatherType;
    }
}