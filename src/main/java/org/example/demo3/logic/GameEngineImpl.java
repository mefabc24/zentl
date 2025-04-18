package org.example.demo3.logic;

import org.example.demo3.model.player.Player;

public class GameEngineImpl implements GameEngine {

    @Override
    public Player determineRoundWinner(Player player1, Player player2, int score1, int score2) {
        if (score1 > score2) {
            return player1;
        } else if (score2 > score1) {
            return player2;
        } else {
            return null;
        }
    }

    @Override
    public Player determineGameWinner(Player player1, Player player2, int wins1, int wins2) {
        if (wins1 > wins2) {
            return player1;
        } else if (wins2 > wins1) {
            return player2;
        } else {
            return null;
        }
    }

}
