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
        // Deck: 30 Karten, Hand: 0 Karten
        player.drawCard();

        assertEquals(1, player.getHand().size());
        assertEquals(29, player.getDeck().size());

        for (int i = 0; i < 29; i++) {
            player.drawCard();
        }

        assertEquals(30, player.getHand().size());
        assertEquals(0, player.getDeck().size());

        // Karte ziehen bei leerem Deck
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
    void testPass() {
        assertFalse(player.hasPassed());

        player.pass();
        assertTrue(player.hasPassed());

        player.resetPass();
        assertFalse(player.hasPassed());
    }

    @Test
    void testPlayCardNotInHand() {
        Board board = new GameBoard();
        Card secretCard = new UnitCard("", 12312, "", Fraction.KNIGHTS, RowType.MELEE);
        player.drawCard();

        assertFalse(player.getHand().contains(secretCard));

        player.playCard(secretCard, board);

        assertEquals(1, player.getHand().size());
        assertTrue(board.getPlayerRows(player).isEmpty());
    }

}
