package org.example.demo3.event;
import org.example.demo3.model.player.Player;

public class InitEve extends Event {
    private final Player player1;
    private final Player player2;

    public InitEve(Player player1, Player player2) {
        this.player1 = player1;
        this.player2 = player2;
    }

    public Player getPlayer1() { return player1; }
    public Player getPlayer2() { return player2; }

    @Override
    public EventType getEventType(){
        return EventType.Init;
    }
}