package org.example.demo3.logic;

import org.example.demo3.model.player.Player;

public class GameEngineImpl implements GameEngine {
    public Player determineRoundWinner(Player player1, Player player2) {
        int score1 = player1.getScore();
        int score2 = player2.getScore();
        if (score1 > score2) {
            return player1;
        } else if (score2 > score1) {
            return player2;
        } else {
            return null;
        }
    }

    public Player determineGameWinner(Player player1, Player player2) {
        int wins1 = player1.getWins();
        int wins2 = player2.getWins();
        if (wins1 > wins2) {
            return player1;
        } else if (wins2 > wins1) {
            return player2;
        } else {
            return null;
        }
    }
}