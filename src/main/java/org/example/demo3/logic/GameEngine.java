package org.example.demo3.logic;

import org.example.demo3.model.player.Player;

public interface GameEngine {
    Player determineRoundWinner(Player player1, Player player2);

    Player determineGameWinner(Player player1, Player player2);
}