package org.example.demo3.event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        Class<? extends Event> eventType = event.getClass();
        if (listeners.containsKey(eventType)) {
            System.out.println("Posting Event: " + event.getName());
            List<EventHandler<? extends Event>> eventListeners = new ArrayList<>(listeners.get(eventType));
            for (EventHandler<? extends Event> listener : eventListeners) {
                ((EventHandler<Event>) listener).execute(event);
            }
        }
    }
}