package org.example.demo3.network;

// wrapper class for network communication => standardizes messages by bundling an event type with its json data
public class NetworkMessage {
    private final String eventType; // the type of the event (z.B. GameStateUpdateEve) => used for deserialization
    private final String jsonData; // the serialized json data of the event object

    public NetworkMessage(String eventType, String jsonData) {
        this.eventType = eventType;
        this.jsonData = jsonData;
    }

    public String getEventType() {return eventType;}

    public String getJsonData() {return jsonData;}
}