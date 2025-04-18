package org.example.demo3.event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EventBus {
    private static final EventBus instanz = new EventBus();
    private final Map<Class<? extends Event>, List<EventHandler<? extends Event>>> listeners = new HashMap<>();

    private EventBus() {
    }

    public static EventBus getInstance() {
        return instanz;
    }

    public <T extends Event> void subscribe(Class<T> eventType, EventHandler<T> listener) {
        List<EventHandler<? extends Event>> list = listeners.computeIfAbsent(eventType, eventTyp -> new ArrayList<>());
        list.add(listener);
    }

    public void post(Event event) {
        Class<?> eventType = event.getClass();
        if (listeners.containsKey(eventType)) {
            List<EventHandler<? extends Event>> eventListeners = new ArrayList<>(listeners.get(eventType));
            for (EventHandler<? extends Event> listener : eventListeners) {
                ((EventHandler<Event>) listener).execute(event);
            }
        }
    }
}