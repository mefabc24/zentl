package org.example.demo3.event;

import org.example.demo3.model.player.Player;

public class PassCommand extends Event {
    private final Player player;

    public PassCommand(Player player) {
        this.player = player;
    }

    public Player getPlayer() {
        return player;
    }
}