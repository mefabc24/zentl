package org.example.demo3.testing;

import org.example.demo3.model.board.Board;
import org.example.demo3.model.board.GameBoard;
import org.example.demo3.model.cards.Card;
import org.example.demo3.model.cards.UnitCard;
import org.example.demo3.model.enums.Faction;
import org.example.demo3.model.enums.Rarity;
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
            deck.add(new UnitCard(i, 4, "b ", "Desc", Faction.KNIGHTS, RowType.RANGED,
                    Rarity.COMMON, "test/paths/ImagePath/", true));
        for (int i = 0; i < 10; i++)
            deck.add(new UnitCard(i+20, 7, "c ", "Desc", Faction.KNIGHTS, RowType.MELEE,
                    Rarity.RARE, "test/paths/ImagePath/", true));
        for (int i = 0; i < 5; i++)
            deck.add(new UnitCard(i+100, 8, "d ", "Desc", Faction.KNIGHTS, RowType.SIEGE,
                    Rarity.RARE, "test/paths/ImagePath/", true));
        Collections.shuffle(deck);

        player = new PlayerImpl("a", Faction.KNIGHTS, deck);

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
        assertTrue(board.getPlayerRows(player).get(card.getRowType()).contains(card));
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
        Card secretCard = new UnitCard(53, 12312, "", "", Faction.KNIGHTS, RowType.MELEE, Rarity.EPIC,
                "test/paths/ImagePath/", true);
        player.drawCard();

        assertFalse(player.getHand().contains(secretCard));

        player.playCard(secretCard, board);

        assertEquals(1, player.getHand().size());
        assertTrue(board.getPlayerRows(player).isEmpty());
    }

}
