package org.example.demo3.event;
import org.example.demo3.model.player.Player;

public class GameEndedEve extends Event {
    private final Player winner;
    private final int player1Wins;
    private final int player2Wins;

    public GameEndedEve(Player winner, int player1Wins, int player2Wins) {
        this.winner = winner;
        this.player1Wins = player1Wins;
        this.player2Wins = player2Wins;
    }

    public Player getWinner() {
        return winner;
    }
    public int getPlayer1Wins() {
        return player1Wins;
    }
    public int getPlayer2Wins() {
        return player2Wins;
    }

}
