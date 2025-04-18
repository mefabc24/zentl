package org.example.demo3.event;

import org.example.demo3.model.player.Player;

public class PlayerChangedEve extends Event {
    private final Player newCurrentPlayer;

    public PlayerChangedEve(Player newCurrentPlayer) {
        this.newCurrentPlayer = newCurrentPlayer;
    }
    public Player getNewCurrentPlayer() {
        return newCurrentPlayer;
    }

}
