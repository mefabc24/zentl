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

    // Private statische Hilfsmethode, um den super()-Konstruktor sauber zu halten
    public static EffectType determineEffectType(WeatherType weatherType) {
        if (weatherType == null) {
            return EffectType.NONE;
        }
        return switch (weatherType) {
            case FROST -> EffectType.WEATHER_FROST;
            case FOG -> EffectType.WEATHER_FOG;
            case RAIN -> EffectType.WEATHER_RAIN;
            // Falls es in Zukunft einen WeatherType ohne Effekt gäbe
            // default -> EffectType.NONE;
        };
    }

    // Get-Set
    public WeatherType getWeatherType() { return weatherType; }
    public void setWeatherType(WeatherType weatherType) { this.weatherType = weatherType; }
}