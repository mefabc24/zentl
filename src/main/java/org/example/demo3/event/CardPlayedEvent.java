package org.example.demo3.event;
import org.example.demo3.model.*;
// gleiches Prinzip fuer alle Event Klassen
public class CardPlayedEvent extends Event {
    //speichern welcher Spieler und welche Karte gespielt wurde
    private final Player player;
    private final Card card;
    // Zum erzeugen eines Events, wir geben die noetigen Infos an, welcher Spieler hat welche Karte gespielt
    public CardPlayedEvent(Player player, Card card) {
        this.player = player;
        this.card = card;
    }
    // Getter, für die Listener
    public Player getPlayer() { return player; }
    public Card getCard() { return card; }
}