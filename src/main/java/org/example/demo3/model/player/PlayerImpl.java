package org.example.demo3.model.player;

import org.example.demo3.model.board.GameBoard;
import org.example.demo3.model.cards.Card;
import org.example.demo3.model.cards.UnitCard;
import org.example.demo3.model.enums.Faction;
import org.example.demo3.model.enums.RowType;

import java.util.*;

public class PlayerImpl implements Player {
    private final String name;
    private final List<Card> deck;
    private final List<Card> hand;
    private boolean passed;
    private final Faction faction;

    public PlayerImpl(String name, Faction faction, List<Card> deck) {
        this.name = name;
        this.faction = faction;
        this.deck = new ArrayList<>(deck);
        this.hand = new ArrayList<>();
        this.passed = false;
        Collections.shuffle(this.deck);
    }

    @Override public String getName() { return name; }
    @Override public Faction getFaction() { return faction; }

    @Override
    public int getScore() {
        return hand.stream().mapToInt(Card::getPower).sum();
    }

    @Override
    public List<Card> getHand() {
        return Collections.unmodifiableList(hand);
    }

    @Override
    public List<Card> getDeck() {
        return Collections.unmodifiableList(deck);
    }

    @Override
    public void drawCard() {
        if (!deck.isEmpty()) {
            hand.add(deck.remove(0));
        }
    }

    @Override
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

    @Override
    public void pass() {
        passed = true;
    }

    @Override
    public boolean hasPassed() {
        return passed;
    }

    @Override
    public boolean canPlay() {
        return !passed;
    }

    @Override
    public void resetPass() {
        passed = false;
    }
}