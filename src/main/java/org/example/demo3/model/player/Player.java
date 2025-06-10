package org.example.demo3.model.player;

import org.example.demo3.model.board.Board;
import org.example.demo3.model.cards.Card;
import org.example.demo3.model.enums.Faction;

import java.util.List;

public interface Player {
    String getName();

    List<Card> getHand();
    List<Card> getDiscardPile();
    void addToDiscardPile(Card card);
    Card removeFromDiscardPile(Card card);
    
    List<Card> getDeck();
    
    boolean removeFromHand(Card card);
    void addToHand(Card card);
    
    void drawCard();

    void playCard(Card card, Board board);

    void pass();

    boolean hasPassed();

    Faction getfraction();

    boolean canPlay();

    void resetPass();

    int getScore();

    void setScore(int score);

    int getWins();

    void setWins(int wins);

    Player copy();

    List<Card> getHandInternal();
}