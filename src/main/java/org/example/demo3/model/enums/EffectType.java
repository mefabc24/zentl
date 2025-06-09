package org.example.demo3.model.enums;

public enum EffectType {
    NONE,

    // Buffs / Debuffs
    COMMANDERS_HORN, // Doubles row power

    // Removal / Destruction
    SCORCH,          // Nukes strongest cards on the field

    // Utility
    MEDIC,           // Revive a card
    CLEAR_WEATHER,   // Removes all weather effects
    RALLY,           // Removes all weather effects from the board (same as Clear Weather)
    DIMERITIUM_BOMB,  // Removes ALL row effects from the board

    // Weather
    WEATHER_FROST,
    WEATHER_FOG,
    WEATHER_RAIN,
}