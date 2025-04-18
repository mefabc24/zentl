package org.example.demo3.event;

import java.util.*;

public class EventBus {
    private static final EventBus instanz = new EventBus();

    // Event ist ein funkt. Interface, es nimmt genau ein arg entgegen und gibt nix zurueck => funktioniert ueber side-effects => bekommt Event Info und fuehrt Aktion aus
    private final Map<EventType, List<EventHandler>> listeners = new HashMap<>();

    private EventBus(){
    }

    public static EventBus getInstance(){
         return instanz;
    }

    public void subscribe(EventType eventType, EventHandler listener){
        List<EventHandler> list = listeners.computeIfAbsent(eventType, eventTyp -> new ArrayList<>());
        list.add(listener);
    }

    public void post(Event event){
        EventType eventType = event.getEventType();
        if (listeners.containsKey(eventType)) {
            List<EventHandler> eventListeners = new ArrayList<>(listeners.get(eventType));
            for (EventHandler listener : eventListeners) {
                try {
                     listener.execute(event);
                } catch (Exception error) {
                    System.out.println("Error fuer Event " + eventType + ": " + error.getMessage());
                }
            }
        }
    }
}
