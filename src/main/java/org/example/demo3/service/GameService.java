package org.example.demo3.service;

import org.example.demo3.model.cards.Card;
import org.example.demo3.model.player.Player;

public interface GameService {
    void initializeGame();

    void playCard(Player player, Card card);

    void playerPass(Player player);

    void startNewRound();
}