package org.example.demo3.service;

import org.example.demo3.event.*;
import org.example.demo3.model.*;
import java.util.function.Consumer;

public interface GameService {

    void initializeGame();

    // Spieler aktionen
    void playCard(Player player, Card card);
    void playerPass(Player player);
    void startNewRound();
    void restartGame();

    // Spielzustaende abfragen
    Player getCurrentPlayer();
    Player getPlayer1();
    Player getPlayer2();
    int getRound();
    int getPlayerScore(Player player);
    boolean isRoundOver();
    int getPlayer1Wins();
    int getPlayer2Wins();
    GameBoard getGameBoard();

    // Event-Handling (korrigiert)
    <T extends Event> void registerEventHandler(Class<T> eventType, Consumer<T> handler);
}
