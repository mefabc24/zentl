package org.example.demo3.testing;

import org.example.demo3.model.board.GameBoard;
import org.example.demo3.model.cards.Card;
import org.example.demo3.model.cards.UnitCard;
import org.example.demo3.model.enums.Faction;
import org.example.demo3.model.enums.Rarity;
import org.example.demo3.model.enums.RowType;
import org.example.demo3.model.player.Player;
import org.example.demo3.model.player.PlayerImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class GameBoardTest {

    private GameBoard board;
    private Player p1;
    private Player p2;

    @BeforeEach
    void setUp() {
        board = new GameBoard();
        p1 = new PlayerImpl("a", Faction.KNIGHTS, new ArrayList<>());
        p2 = new PlayerImpl("b", Faction.KNIGHTS, new ArrayList<>());
    }

    @Test
    void testAddCardToRow() {
        Card card = new UnitCard(1, 12, "Soldier ", "Test Description", Faction.KNIGHTS, RowType.MELEE,
                Rarity.EPIC, "test/paths/ImagePath/", true);
        board.addCardToRow(card, p1);

        // wurde Karte hinzugefuegt?
        assertTrue(board.getPlayerRows(p1).containsKey(RowType.MELEE));
        // wurde mehr als nur eine Karte hi nzugefuegt?
        assertEquals(1, board.getPlayerRows(p1).get(RowType.MELEE).size());
        // stimmt die erste Karte mit unser Karte ueberein?
        assertEquals(card, board.getPlayerRows(p1).get(RowType.MELEE).getFirst());
        // wurde bei Spieler 2 etwas hinzugefuegt?
        assertTrue(board.getPlayerRows(p2).isEmpty());

    }

    @Test
    void testCalcPower() {

        Card card1 = new UnitCard(2, 3, "a " , "Desc", Faction.KNIGHTS, RowType.MELEE,
                Rarity.COMMON, "test/paths/ImagePath/", true);
        Card card2 = new UnitCard(3, 4, "b ", "Desc", Faction.KNIGHTS, RowType.RANGED,
                Rarity.COMMON, "test/paths/ImagePath/", true);
        Card card3 = new UnitCard(4, 7, "c ", "Desc", Faction.KNIGHTS, RowType.MELEE,
                Rarity.RARE, "test/paths/ImagePath/", true);
        Card card4 = new UnitCard(5, 8, "d ", "Desc", Faction.KNIGHTS, RowType.SIEGE,
                Rarity.RARE, "test/paths/ImagePath/", true);

        board.addCardToRow(card1, p1);
        board.addCardToRow(card2, p1);
        board.addCardToRow(card3, p1);
        board.addCardToRow(card4, p1);

        assertEquals(22, board.calculateTotalPower(p1));
        assertEquals(0, board.calculateTotalPower(p2));
        board.clearBoard();
        assertEquals(0, board.calculateTotalPower(p1));
        assertEquals(0, board.calculateTotalPower(p2));
    }


}
