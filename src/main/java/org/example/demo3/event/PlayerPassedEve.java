package org.example.demo3.event;

import org.example.demo3.model.player.Player;

public class PlayerPassedEve extends Event {
    private final Player player;

    public PlayerPassedEve(Player player) {
        this.player = player;
    }

    public Player getPlayer() {
        return player;
    }

}