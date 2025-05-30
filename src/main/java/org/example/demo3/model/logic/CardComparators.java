package org.example.demo3.model.logic;

import org.example.demo3.model.cards.Card;
import org.example.demo3.model.cards.CardInstance;

import java.util.Comparator;

public class CardComparators {

    // by Rarity (CardInstance)
    public static final Comparator<CardInstance> BY_RARITY_ASC_INSTANCE =
            Comparator.comparingInt((CardInstance ci) -> ci.getCardDefinition().getRarity().getSortID());
    public static final Comparator<CardInstance> BY_RARITY_DESC_INSTANCE = BY_RARITY_ASC_INSTANCE.reversed();

    // by Power (CardInstance)
    public static final Comparator<CardInstance> BY_POWER_ASC_INSTANCE =
            Comparator.comparingInt((CardInstance ci) -> ci.getCardDefinition().getPower());
    public static final Comparator<CardInstance> BY_POWER_DESC_INSTANCE = BY_POWER_ASC_INSTANCE.reversed();

    // by RowType (CardInstance)
    public static final Comparator<CardInstance> BY_ROWTYPE_ASC_INSTANCE =
            Comparator.comparingInt((CardInstance ci) -> ci.getCardDefinition().getRowType().getSortID());
    public static final Comparator<CardInstance> BY_ROWTYPE_DESC_INSTANCE = BY_ROWTYPE_ASC_INSTANCE.reversed();

    // by CardType (CardInstance)
    public static final Comparator<CardInstance> BY_CARDTYPE_ASC_INSTANCE =
            Comparator.comparingInt((CardInstance ci) -> ci.getCardDefinition().getCardType().getSortID());
    public static final Comparator<CardInstance> BY_CARDTYPE_DESC_INSTANCE = BY_CARDTYPE_ASC_INSTANCE.reversed();

    // by ID (CardInstance)
    public static final Comparator<CardInstance> BY_ID_ASC_INSTANCE =
            Comparator.comparingInt((CardInstance ci) -> ci.getCardDefinition().getId())
                    .thenComparing(CardInstance::getInstanceId);


    // Sortierung für Card
    public static final Comparator<Card> BY_RARITY_ASC = Comparator.comparingInt(c -> c.getRarity().getSortID());
    public static final Comparator<Card> BY_POWER_ASC = Comparator.comparingInt(Card::getPower);
    public static final Comparator<Card> BY_ROWTYPE_ASC = Comparator.comparing(c -> c.getRowType().getSortID());
    public static final Comparator<Card> BY_CARDTYPE_ASC = Comparator.comparing(c -> c.getCardType().getSortID());
    public static final Comparator<Card> BY_ID_ASC = Comparator.comparingInt(Card::getId);

}