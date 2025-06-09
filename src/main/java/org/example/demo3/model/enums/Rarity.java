package org.example.demo3.model.enums;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public enum Rarity {
    COMMON (0, 100, 0.5),
    RARE (1, 20, 0.3),
    EPIC (2, 5, 0.15),
    LEGENDARY (3, 1, 0.04),
    MYTHIC (4, 1, 0.01);

    private final int sortID;
    private final int maxAmount;
    private final double weight;

    private static final List<Rarity> VALUES = List.of(values());
    private static final double TOTAL_WEIGHT = VALUES.stream().mapToDouble(Rarity::getWeight).sum();

    Rarity(int sortID, int maxAmount, double weight) {
        this.sortID = sortID;
        this.maxAmount = maxAmount;
        this.weight = weight;
    }

    // Getter
    public int getSortID() { return sortID; }
    public int getMaxAmount() { return maxAmount; }
    public double getWeight() { return weight; }

    // Sucht passende Rarity anhand der SortID
    public static Rarity fromSortID(int id) {
        for (Rarity rarity : VALUES) { // VALUES ist die Liste, die du schon hast
            if (rarity.getSortID() == id) {
                return rarity;
            }
        }
        // Dieser Fehlerfall ist wichtig, um Probleme im Code schnell zu finden.
        throw new IllegalArgumentException("Keine Rarity mit der sortID gefunden: " + id);
    }

    // Static Methode fuer Zufallsauswahl
    public static Rarity getRandom() {
        double random = ThreadLocalRandom.current().nextDouble(TOTAL_WEIGHT);
        double cumulativeWeight = 0.0;
        System.out.println(random);

        for (Rarity rarity : VALUES) {
            cumulativeWeight += rarity.getWeight();
            if (cumulativeWeight > random) {
                System.out.println(rarity);
                return rarity;
            }
        }
        throw new IllegalStateException("Konnte keine Seltenheit auswählen. Überprüfe die Gewichte.");
    }
}
