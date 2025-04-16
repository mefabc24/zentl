package org.example.demo3.event;

import org.example.demo3.model.*;

public class PlayerPassedEvent extends Event {
    private final Player player;

    public PlayerPassedEvent(Player player) {
        this.player = player;
    }

    public Player getPlayer() { return player; }
}