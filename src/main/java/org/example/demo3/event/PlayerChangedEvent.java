package org.example.demo3.event;

import org.example.demo3.model.Player;

public class PlayerChangedEvent extends Event {
    private final Player newCurrentPlayer;

    public PlayerChangedEvent(Player newCurrentPlayer) {
        this.newCurrentPlayer = newCurrentPlayer;
    }
    // Getter noch nicht genutzt aber sinnvoll, aktuell nur GameController, bekommt den aktuellen Spieler von GameService(Single Source of trust), da er die ganze UI updated und den ganzen Zustand braucht und nicht nur den aktuellen Spieler. Sinnvoll aber fuer spaetere Listener die nur am Spielerwechsel interessiert sind
    public Player getNewCurrentPlayer() {
        return newCurrentPlayer;
    }
}
