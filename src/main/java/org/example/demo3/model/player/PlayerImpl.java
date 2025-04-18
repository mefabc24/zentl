package org.example.demo3.model.player;

import org.example.demo3.model.board.GameBoard;
import org.example.demo3.model.cards.Card;
import org.example.demo3.model.cards.UnitCard;
import org.example.demo3.model.enums.Fraction;
import org.example.demo3.model.enums.RowType;

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
        this.deck = new ArrayList<>(deck);
        this.hand = new ArrayList<>();
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
            hand.add(deck.remove(0));
        }
    }

    public void playCard(Card card, GameBoard board) {
        if (hand.contains(card)) {
            hand.remove(card);
            if (card instanceof UnitCard) {
                board.addCardToRow(card, ((UnitCard) card).getRow(), this);
            } else {
                board.addCardToRow(card, RowType.ANY, this);
            }
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