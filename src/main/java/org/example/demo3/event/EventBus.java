package org.example.demo3.event;

import java.util.*;

public class EventBus {

    private static final EventBus instanz = new EventBus();
    private final Map<Class<? extends Event>, List<EventHandler<? extends Event>>> listeners = new HashMap<>();

    private EventBus() {}

    public static EventBus getInstanz() {
        return instanz;
    }

    public <T extends Event> void subscribe(Class<T> eventType, EventHandler<T> listener) {
        listeners.computeIfAbsent(eventType, k -> new ArrayList<>()).add(listener);
    }

    public <T extends Event> void unsubscribe(Class<T> eventType, EventHandler<T> listener) {
        List<EventHandler<? extends Event>> eventHandlers = listeners.get(eventType);
        if (eventHandlers != null) {
            eventHandlers.remove(listener);
        }
    }

    @SuppressWarnings("unchecked cast")
    public void post(Event event) {
        List<EventHandler<? extends Event>> handlers = new ArrayList<>(listeners.getOrDefault(event.getClass(), Collections.emptyList()));
        for (EventHandler<? extends Event> listener : handlers) {
            try {
                ((EventHandler<Event>) listener).execute(event);
                // Optional: Zum Debuggen wieder einkommentieren
                // System.out.println("Executing Event: " + event.getName() + " for listener " + listener.getClass().getSimpleName());
            } catch (Exception e) {
                System.err.println("Error executing event " + event.getName() + " for listener " + listener);
                e.printStackTrace();
            }
        }
    }
}