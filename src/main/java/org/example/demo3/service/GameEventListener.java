package org.example.demo3.service;
import org.example.demo3.model.*;

public interface GameEventListener {
    void onGameInitialized();
    void onCardPlayed(Player player, Card card);
    void onPlayerPassed(Player player);
    void onRoundEnded(Player roundWinner, int p1Score, int p2Score);
    void onGameEnded(Player gameWinner, int p1Wins, int p2Wins);
    void onPlayerChanged(Player newCurrentPlayer);
}
