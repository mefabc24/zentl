package org.example.demo3.event;

public class PlayerDisconnectedEvent extends Event {
    private final String message;

    public PlayerDisconnectedEvent(String message) {
        this.name = "PlayerDisconnectedEvent";
        this.message = message;
    }

    public String getMessage() {return message;}
}