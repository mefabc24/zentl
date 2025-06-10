package org.example.demo3.event;

public class EffectLogEvent extends Event {
    private final String message;

    public EffectLogEvent(String message) {
        this.name = "EffectLogEvent";
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}