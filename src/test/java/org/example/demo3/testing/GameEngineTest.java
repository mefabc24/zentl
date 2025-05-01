package org.example.demo3.testing;

import org.example.demo3.model.logic.GameEngine;
import org.example.demo3.model.player.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

// Wir nutzen JUnit5 und Mockito fuer unsere Unit Tests
@ExtendWith(MockitoExtension.class)
public class GameEngineTest {
    // man @Mock wenn man die Logik des Objekts nicht braucht und man das Objekt nur simulieren will
    @Mock
    private Player p1Mock;
    @Mock
    private Player p2Mock;

    private GameEngine gameEngine;

    @BeforeEach
    void setUp() {
        gameEngine = new GameEngine();
    }

    @Test
    void testP1RoundWinner() {
        // wir legen fest was bei getScore() returned werden soll, p1 ist ein Mock, kein echter Player
        when(p1Mock.getScore()).thenReturn(23);
        when(p2Mock.getScore()).thenReturn(22);

        Player winner = gameEngine.determineRoundWinner(p1Mock, p2Mock);
        assertNotNull(winner);
        assertEquals(p1Mock, winner);
    }

    @Test
    void testP2RoundWinner() {
        when(p1Mock.getScore()).thenReturn(25);
        when(p2Mock.getScore()).thenReturn(40);

        Player winner = gameEngine.determineRoundWinner(p1Mock, p2Mock);

        assertNotNull(winner);
        assertEquals(p2Mock, winner);
    }

    @Test
    void testDrawRound() {
        when(p1Mock.getScore()).thenReturn(25);
        when(p2Mock.getScore()).thenReturn(25);

        Player winner = gameEngine.determineRoundWinner(p1Mock, p2Mock);

        assertNull(winner);
    }

    @Test
    void testP1GameWinner() {
        when(p1Mock.getWins()).thenReturn(2);
        when(p2Mock.getWins()).thenReturn(1);

        Player winner = gameEngine.determineGameWinner(p1Mock, p2Mock);

        assertNotNull(winner);
        assertEquals(p1Mock, winner);
    }

    @Test
    void testP2GameWinner() {
        when(p1Mock.getWins()).thenReturn(0);
        when(p2Mock.getWins()).thenReturn(2);

        Player winner = gameEngine.determineGameWinner(p1Mock, p2Mock);

        assertNotNull(winner);
        assertEquals(p2Mock, winner);
    }

    @Test
    void testDrawGame() {
        when(p1Mock.getWins()).thenReturn(2);
        when(p2Mock.getWins()).thenReturn(2);

        Player winner = gameEngine.determineGameWinner(p1Mock, p2Mock);

        assertNull(winner);
    }

}
