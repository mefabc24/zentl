package org.example.demo3.model.player;

import org.example.demo3.model.board.Board;
import org.example.demo3.model.cards.Card;
import org.example.demo3.model.enums.Fraction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PlayerImpl implements Player {
    private final String name;
    private final List<Card> deck;
    private final List<Card> hand;
    private final Fraction fraction;
    private int score;
    private int wins;
    private boolean passed;

    public PlayerImpl(String name, Fraction fraction, List<Card> deck) {
        this.name = name;
        this.fraction = fraction;
        this.deck = new ArrayList<Card>(deck);
        this.hand = new ArrayList<Card>();
        this.passed = false;
        Collections.shuffle(this.deck);
        this.score = 0;
        this.wins = 0;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public int getWins() {
        return wins;
    }

    public void setWins(int wins) {
        this.wins = wins;
    }

    public String getName() {
        return name;
    }

    public Fraction getfraction() {
        return fraction;
    }

    public List<Card> getHand() {
        return Collections.unmodifiableList(hand);
    }

    public List<Card> getDeck() {
        return Collections.unmodifiableList(deck);
    }

    public void drawCard() {
        if (!deck.isEmpty()) {
            hand.add(deck.removeFirst());
        }

    }

    public void playCard(Card card, Board board) {
        if (hand.contains(card)) {
            hand.remove(card);
            board.addCardToRow(card, this);
        }
    }

    public void pass() {
        passed = true;
    }

    public boolean hasPassed() {
        return passed;
    }

    public boolean canPlay() {
        return !passed;
    }

    public void resetPass() {
        passed = false;
    }

}