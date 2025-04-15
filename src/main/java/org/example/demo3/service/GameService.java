package org.example.demo3.service;
import org.example.demo3.model.*;

public interface GameService {

    void initializeGame();

    // Spieler aktionen
    void playCard(Player player, Card card);
    void playerPass(Player player);
    void startNewRound();
    void restartGame();

    // Spielzustaende abfragen
    Player getCurrentPlayer();
    Player getPlayer1();  // Hinzugefügt
    Player getPlayer2();  // Hinzugefügt
    int getRound();
    int getPlayerScore(Player player);
    boolean isRoundOver();
    int getPlayer1Wins();  // Hinzugefügt
    int getPlayer2Wins();  // Hinzugefügt
    GameBoard getGameBoard();  // Hinzugefügt

    // Event-Handling
    void registerListener(EventListener listener);
}
