package org.example.demo3.event;

public interface EventHandler<T extends Event> {
    void execute(Event event);
}
