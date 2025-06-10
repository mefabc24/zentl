package org.example.demo3.model.player;

import org.example.demo3.model.board.Board;
import org.example.demo3.model.cards.Card;
import org.example.demo3.model.enums.Faction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class PlayerImpl implements Player {
    private final String name;
    private final List<Card> deck;
    private final List<Card> hand;
    private final List<Card> discardPile;
    private final Faction fraction;
    private int score;
    private int wins;
    private boolean passed;

    public PlayerImpl(String name, Faction fraction, List<Card> deck) {
        this.name = name;
        this.fraction = fraction;
        this.deck = new ArrayList<Card>(deck);
        this.hand = new ArrayList<Card>();
        this.discardPile = new ArrayList<>();
        this.passed = false;
        Collections.shuffle(this.deck);
        this.score = 0;
        this.wins = 0;
    }

    // internal use for simulation (deepcopy)
    protected PlayerImpl(PlayerImpl other) {
        this.name = other.name;
        this.fraction = other.fraction;
        this.deck = new ArrayList<>(other.deck);
        this.hand = new ArrayList<>(other.hand);
        this.discardPile = new ArrayList<>(other.discardPile);
        this.passed = other.passed;
        this.score = other.score;
        this.wins = other.wins;
    }

    @Override
    public Player copy() {return new PlayerImpl(this);}

    @Override
    public List<Card> getHandInternal() {return this.hand;}

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

    public Faction getfraction() {
        return fraction;
    }

    public List<Card> getHand() {return Collections.unmodifiableList(hand);}

    public List<Card> getDeck() {
        return Collections.unmodifiableList(deck);
    }
    
    public List<Card> getDiscardPile() {
        return Collections.unmodifiableList(discardPile);
    }

    @Override
    public void addToDiscardPile(Card card) {
        if (card != null) {
            this.discardPile.add(card);
        }
    }

    @Override
    public Card removeFromDiscardPile(Card card) {
        if (card != null && this.discardPile.remove(card)) {
            return card;
        }
        return null;
    }

    @Override
    public void addToHand(Card card) {
        if (card != null) {
            this.hand.add(card);
        }
    }
    
    @Override
    public boolean removeFromHand(Card card) {
        return this.hand.remove(card);
    }

    public void drawCard() {
        if (!deck.isEmpty()) {
            addToHand(deck.removeFirst());
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PlayerImpl player = (PlayerImpl) o;
        return Objects.equals(name, player.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}