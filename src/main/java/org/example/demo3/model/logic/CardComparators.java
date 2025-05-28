package org.example.demo3.model.logic;

import org.example.demo3.model.cards.Card;
import java.util.Comparator;

public class CardComparators {
    private static final Comparator<Card> BY_UNLOCKED_STATUS =
            Comparator.comparingInt(card -> card.isUnlocked() ? 0 : 1);

    // By Power
    public static final Comparator<Card> BY_POWER_ASC =
            BY_UNLOCKED_STATUS.thenComparingInt(Card::getPower);
    public static final Comparator<Card> BY_POWER_DESC =
            BY_UNLOCKED_STATUS.thenComparing(Comparator.comparingInt(Card::getPower).reversed());

    // By Rarity
    public static final Comparator<Card> BY_RARITY_ASC =
            Comparator.comparingInt((Card card) -> card.isUnlocked() ? 1 : 0)
                    .thenComparingInt(card -> card.getRarity().getSortID());
    public static final Comparator<Card> BY_RARITY_DESC =
            BY_UNLOCKED_STATUS.thenComparing(Comparator.comparingInt((Card card) -> card.getRarity().getSortID()).reversed());

    // By CardType
    public static final Comparator<Card> BY_CARDTYPE_ASC =
            BY_UNLOCKED_STATUS
                    .thenComparingInt(card -> card.getCardType().getSortID())
                    .thenComparingInt(card -> card.getRarity().getSortID());
    public static final Comparator<Card> BY_CARDTYPE_DESC =
            BY_UNLOCKED_STATUS
                    .thenComparing(Comparator.comparingInt((Card card) -> card.getCardType().getSortID()).reversed())
                    .thenComparing(Comparator.comparingInt((Card card) -> card.getRarity().getSortID()).reversed());

    // By RowType
    public static final Comparator<Card> BY_ROWTYPE_ASC =
            BY_UNLOCKED_STATUS
                    .thenComparingInt(card -> card.getRowType().getSortID());
    public static final Comparator<Card> BY_ROWTYPE_DESC =
            BY_UNLOCKED_STATUS
                    .thenComparing(Comparator.comparingInt((Card card) -> card.getRowType().getSortID()).reversed());

    // By Name
    public static final Comparator<Card> BY_NAME_ASC =
            BY_UNLOCKED_STATUS
                    .thenComparing(Card::getName, String.CASE_INSENSITIVE_ORDER);
    public static final Comparator<Card> BY_NAME_DESC =
            BY_UNLOCKED_STATUS
                    .thenComparing(Comparator.comparing(Card::getName, String.CASE_INSENSITIVE_ORDER).reversed());

    // By ID
    public static final Comparator<Card> BY_ID_ASC = Comparator.comparingInt(Card::getId);










}
