package org.example.demo3.model.logic;

import org.example.demo3.model.cards.Card;
import org.example.demo3.model.cards.CardInstance;

import java.util.Comparator;

public class CardComparators {

    // by Rarity (CardInstance)
    public static final Comparator<CardInstance> BY_RARITY_ASC_INSTANCE =
            Comparator.comparingInt((CardInstance ci) -> ci.getCardDefinition().getRarity().getSortID());

    // by Power (CardInstance)
    public static final Comparator<CardInstance> BY_POWER_ASC_INSTANCE =
            Comparator.comparingInt((CardInstance ci) -> ci.getCardDefinition().getPower());

    // by RowType (CardInstance)
    public static final Comparator<CardInstance> BY_ROWTYPE_ASC_INSTANCE =
            Comparator.comparingInt((CardInstance ci) -> ci.getCardDefinition().getRowType().getSortID());

    // by CardType (CardInstance)
    public static final Comparator<CardInstance> BY_CARDTYPE_ASC_INSTANCE =
            Comparator.comparingInt((CardInstance ci) -> ci.getCardDefinition().getCardType().getSortID());

    // by ID (CardInstance)
    public static final Comparator<CardInstance> BY_ID_ASC_INSTANCE =
            Comparator.comparingInt((CardInstance ci) -> ci.getCardDefinition().getId())
                    .thenComparing(CardInstance::getInstanceId);


    // Sortierung für Card
    public static final Comparator<Card> BY_RARITY_ASC = Comparator.comparing(Card::getRarity);
    public static final Comparator<Card> BY_POWER_ASC = Comparator.comparingInt(Card::getPower);
    public static final Comparator<Card> BY_ROWTYPE_ASC = Comparator.comparing(Card::getRowType);
    public static final Comparator<Card> BY_CARDTYPE_ASC = Comparator.comparing(Card::getCardType);
    public static final Comparator<Card> BY_ID_ASC = Comparator.comparingInt(Card::getId);

}