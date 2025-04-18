package org.example.demo3.model.player;

import org.example.demo3.model.board.GameBoard;
import org.example.demo3.model.cards.Card;
import org.example.demo3.model.enums.Fraction;

import java.util.List;

public interface Player {
    String getName();

    List<Card> getHand();

    List<Card> getDeck();

    void drawCard();

    void playCard(Card card, GameBoard board);

    void pass();

    boolean hasPassed();

    Fraction getfraction();

    boolean canPlay();

    void resetPass();

    int getScore();

    void setScore(int score);

    int getWins();

    void setWins(int wins);
}