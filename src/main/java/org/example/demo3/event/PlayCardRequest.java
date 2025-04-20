package org.example.demo3.event;

import org.example.demo3.model.cards.Card;
import org.example.demo3.model.player.Player;

public class PlayCardRequest extends Event {
    private final Player player;
    private final Card card;

    public PlayCardRequest(Player player, Card card) {
        this.player = player;
        this.card = card;
        this.name = "PlayCardRequest";
    }

    public Player getPlayer() {
        return player;
    }

    public Card getCard() {
        return card;
}
}