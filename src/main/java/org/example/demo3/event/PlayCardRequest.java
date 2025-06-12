package org.example.demo3.event;

import org.example.demo3.model.cards.Card;

public class PlayCardRequest extends Event {
    private final Card card;

    public PlayCardRequest(Card card) {
        this.card = card;
        this.name = "PlayCardRequest";
    }

    public Card getCard() {
        return card;
    }

}