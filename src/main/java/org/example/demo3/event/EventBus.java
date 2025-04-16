package org.example.demo3.event;

import java.util.*;
import java.util.function.Consumer;

public class EventBus {
    private static EventBus instance;
    private final Map<Class<? extends Event>, List<Consumer<? extends Event>>> listeners = new HashMap<>();

    private EventBus() {}

    public static synchronized EventBus getInstance() {
        if (instance == null) {
            instance = new EventBus();
        }
        return instance;
    }

    // Methode zum Registrieren eines Listeners für einen bestimmten Event-Typ
    public <T extends Event> void subscribe(Class<T> eventType, Consumer<T> listener) {
        listeners.computeIfAbsent(eventType, k -> new ArrayList<>())
                .add(listener);
    }

    // Methode zum Abmelden eines Listeners
    public <T extends Event> void unsubscribe(Class<T> eventType, Consumer<T> listener) {
        if (listeners.containsKey(eventType)) {
            listeners.get(eventType).remove(listener);
        }
    }

    // Methode zum Veröffentlichen eines Events
    @SuppressWarnings("unchecked")
    public <T extends Event> void post(T event) {
        if (listeners.containsKey(event.getClass())) {
            List<Consumer<? extends Event>> eventListeners = listeners.get(event.getClass());
            for (Consumer<? extends Event> listener : eventListeners) {
                ((Consumer<T>) listener).accept(event);
            }
        }
    }
}
