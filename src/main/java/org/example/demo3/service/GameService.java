package org.example.demo3.service;
import org.example.demo3.model.*;

public interface GameService {
    // Spiel initialisieren
    void initializeGame();

    // Spielaktionen
    void playCard(Player player, Card card);
    void playerPass(Player player);
    void startNewRound();
    void restartGame();

    // Spielzustand abfragen
    Player getCurrentPlayer();
    Player getPlayer1();  // Hinzugefügt
    Player getPlayer2();  // Hinzugefügt
    int getRound();
    int getPlayerScore(Player player);
    boolean isRoundOver();
    boolean isGameOver();
    int getPlayer1Wins();  // Hinzugefügt
    int getPlayer2Wins();  // Hinzugefügt
    GameBoard getGameBoard();  // Hinzugefügt

    // Event-Handling
    void registerListener(GameEventListener listener);
    void unregisterListener(GameEventListener listener);
}
