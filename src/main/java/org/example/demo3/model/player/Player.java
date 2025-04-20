package org.example.demo3.model.player;

import org.example.demo3.model.board.Board;
import org.example.demo3.model.cards.Card;
import org.example.demo3.model.enums.Fraction;

import java.util.List;

public interface Player {
    String getName();

    List<Card> getHand();

    List<Card> getDeck();

    void drawCard();

    void playCard(Card card, Board board);

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