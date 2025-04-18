package org.example.demo3.event;

import org.example.demo3.model.player.Player;

public class RoundEndedEve extends Event {
    private final Player winner;
    private final int player1Score;
    private final int player2Score;

    public RoundEndedEve(Player winner, int player1Score, int player2Score) {
        this.winner = winner;
        this.player1Score = player1Score;
        this.player2Score = player2Score;

    }

    public Player getWinner() {
        return winner;
    }

    public int getPlayer1Score() {
        return player1Score;
    }

    public int getPlayer2Score() {
        return player2Score;
    }

    @Override
    public EventType getEventType(){
        return EventType.RoundEnded;
    }
}

