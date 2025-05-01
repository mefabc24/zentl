package org.example.demo3.testing;

import org.example.demo3.model.board.Board;
import org.example.demo3.model.board.GameBoard;
import org.example.demo3.model.cards.Card;
import org.example.demo3.model.cards.UnitCard;
import org.example.demo3.model.enums.Fraction;
import org.example.demo3.model.enums.RowType;
import org.example.demo3.model.player.PlayerImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class PlayerTest {

    private PlayerImpl player;

    @BeforeEach
    void setUp() {
        List<Card> deck = new ArrayList<>();
        for (int i = 0; i < 15; i++)
            deck.add(new UnitCard("Soldier " + (i + 1), 5, "", Fraction.KNIGHTS, RowType.MELEE));
        for (int i = 0; i < 10; i++)
            deck.add(new UnitCard("Archer " + (i + 1), 4, "", Fraction.KNIGHTS, RowType.RANGED));
        for (int i = 0; i < 5; i++)
            deck.add(new UnitCard("Catapult " + (i + 1), 6, "", Fraction.KNIGHTS, RowType.SIEGE));
        Collections.shuffle(deck);

        player = new PlayerImpl("a", Fraction.KNIGHTS, deck);

    }

    @Test
    void testDrawCardsFromDeck() {
        player.drawCard();

        assertEquals(1, player.getHand().size());
        assertEquals(29, player.getDeck().size());

        for (int i = 0; i < 29; i++) {
            player.drawCard();
        }

        assertEquals(30, player.getHand().size());
        assertEquals(0, player.getDeck().size());

        player.drawCard();

        assertEquals(30, player.getHand().size());
        assertEquals(0, player.getDeck().size());
    }

    @Test
    void testCardPlayed() {
        Board board = new GameBoard();
        player.drawCard();
        Card card = player.getHand().getFirst();

        assertEquals(1, player.getHand().size());

        player.playCard(card, board);

        assertEquals(0, player.getHand().size());
        assertTrue(board.getPlayerRows(player).get(card.getRow()).contains(card));
    }

    @Test
    void testPassAndResetPass() {
        assertFalse(player.hasPassed());

        player.pass();
        assertTrue(player.hasPassed());

        player.resetPass();
        assertFalse(player.hasPassed());
    }
}
