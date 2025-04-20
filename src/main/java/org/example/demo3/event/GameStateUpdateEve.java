package org.example.demo3.event;

import org.example.demo3.model.board.Board;
import org.example.demo3.model.player.Player;

public class GameStateUpdateEve extends Event {
    private final Player player1;
    private final Player player2;
    private final Player currentPlayer;
    private final int round;
    private final Board gameBoard;

    public GameStateUpdateEve(Player player1, Player player2, Player currentPlayer, int round, Board gameBoard) {
        this.name = "GameStateUpdate";
        this.player1 = player1;
        this.player2 = player2;
        this.currentPlayer = currentPlayer;
        this.round = round;
        this.gameBoard = gameBoard;
    }

    public Player getPlayer1() {
        return player1;
    }

    public Player getPlayer2() {
        return player2;
    }

    public Player getCurrentPlayer() {
        return currentPlayer;
    }

    public int getRound() {
        return round;
    }

    public Board getGameBoard() {
        return gameBoard;
    }
}