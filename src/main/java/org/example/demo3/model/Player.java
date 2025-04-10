package org.example.demo3.model;

import org.example.demo3.model.enums.Faction;

import java.util.List;

public interface Player {
    String getName();
    int getScore();
    List<Card> getHand();
    List<Card> getDeck();
    void drawCard();
    void playCard(Card card, GameBoard board);
    void pass();
    boolean hasPassed();
    Faction getFaction();
    boolean canPlay();
    void resetPass();
}