package org.example.demo3.model.logic;

import org.example.demo3.model.player.Player;

public interface Engine {
    Player determineRoundWinner(Player player1, Player player2);

    Player determineGameWinner(Player player1, Player player2);
}