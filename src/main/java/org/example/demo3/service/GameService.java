package org.example.demo3.service;
import org.example.demo3.model.board.GameBoard;
import org.example.demo3.model.cards.Card;
import org.example.demo3.model.player.Player;

public interface GameService {

    void initializeGame();
    void playCard(Player player, Card card);
    void playerPass(Player player);
    void startNewRound();
    void restartGame();

    Player getCurrentPlayer();
    Player getPlayer1();
    Player getPlayer2();
    int getRound();
    int getPlayerScore(Player player);
    boolean isRoundOver();
    int getPlayer1Wins();
    int getPlayer2Wins();
    GameBoard getGameBoard();

}
