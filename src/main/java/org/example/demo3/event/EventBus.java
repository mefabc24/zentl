package org.example.demo3.event;

import java.util.*;

public class EventBus {
    private static final EventBus instanz = new EventBus();

    private EventBus() {}

    public static EventBus getInstance() {
        return instanz;
    }

    private final Map<Class<? extends Event>, List<EventHandler<? extends Event>>> listeners = new HashMap<>();

    public <T extends Event> void subscribe(Class<T> eventType, EventHandler<T> listener) {
        listeners.computeIfAbsent(eventType, eventTyp -> new ArrayList<>())
                .add(listener);
    }

    public void post(Event event) {
        for (EventHandler<? extends Event> listener : listeners.getOrDefault(event.getClass(), Collections.emptyList())) {
            ((EventHandler<Event>) listener).execute(event);
            System.out.println("Executing Event: " + event.getName());
        }
    }
}

