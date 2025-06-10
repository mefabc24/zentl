package org.example.demo3.model.service;

import org.example.demo3.model.enums.DropType;
import org.example.demo3.model.enums.Rarity;

import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class RarityDropService {
    private static final RarityDropService INSTANCE = new RarityDropService();

    private final Map<DropType, Map<Rarity, Double>> probabilityTables;

    private RarityDropService() {
        probabilityTables = new EnumMap<>(DropType.class);

        // --- Basic Drop Wahrscheinlichkeiten ---
        Map<Rarity, Double> basicChances = new EnumMap<>(Rarity.class);
        basicChances.put(Rarity.COMMON, 0.50);      // 50%
        basicChances.put(Rarity.RARE, 0.30);        // 30%
        basicChances.put(Rarity.EPIC, 0.15);        // 15%
        basicChances.put(Rarity.LEGENDARY, 0.04);   //  4%
        basicChances.put(Rarity.MYTHIC, 0.01);      //  1%
        this.probabilityTables.put(DropType.BASIC, basicChances);

        // --- Premium Drop Wahrscheinlichkeiten ---
        Map<Rarity, Double> premiumChances = new EnumMap<>(Rarity.class);
        premiumChances.put(Rarity.COMMON, 0.15);      // 15%
        premiumChances.put(Rarity.RARE, 0.30);        // 30%
        premiumChances.put(Rarity.EPIC, 0.40);        // 40%
        premiumChances.put(Rarity.LEGENDARY, 0.10);   // 10%
        premiumChances.put(Rarity.MYTHIC, 0.05);      //  5%
        this.probabilityTables.put(DropType.PREMIUM, premiumChances);

        // --- Elite Drop Wahrscheinlichkeiten ---
        Map<Rarity, Double> eliteChances = new EnumMap<>(Rarity.class);
        eliteChances.put(Rarity.COMMON, 0.00);      //  0%
        eliteChances.put(Rarity.RARE, 0.00);        //  0%
        eliteChances.put(Rarity.EPIC, 0.30);        // 30%
        eliteChances.put(Rarity.LEGENDARY, 0.60);   // 60%
        eliteChances.put(Rarity.MYTHIC, 0.10);      // 10%
        this.probabilityTables.put(DropType.ELITE, eliteChances);
    }

    public Map<Rarity, Double> getProbabilityTable(DropType type) {
        return probabilityTables.get(type);
    }

    public static RarityDropService getInstance() {
        return INSTANCE;
    }

    public Rarity getRandomRarity(DropType dropType) {
        Map<Rarity, Double> chances = probabilityTables.get(dropType);
        if (chances == null) {
            throw new IllegalArgumentException("Keine Wahrscheinlichkeitstabelle für DropType definiert: " + dropType);
        }

        double totalWeight = chances.values().stream().mapToDouble(Double::doubleValue).sum();
        if (totalWeight <= 0) {
            throw new IllegalStateException("Gesamtgewicht für " + dropType + " ist 0. Keine Ziehung möglich.");
        }

        double random = ThreadLocalRandom.current().nextDouble(totalWeight);
        double cumulativeWeight = 0.0;

        for (Map.Entry<Rarity, Double> entry : chances.entrySet()) {
            cumulativeWeight += entry.getValue();
            if (cumulativeWeight > random) {
                return entry.getKey();
            }
        }

        // Fallback
        throw new IllegalStateException("Konnte keine Seltenheit auswählen für " + dropType);
    }

}
