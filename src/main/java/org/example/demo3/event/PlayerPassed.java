package org.example.demo3.event;

import org.example.demo3.model.player.Player;

public class PlayerPassed extends Event {
    private final Player player;

    public PlayerPassed(Player player) {
        this.player = player;
        this.name = "PlayerPassed";
    }

    public Player getPlayer() {
        return player;
    }
}