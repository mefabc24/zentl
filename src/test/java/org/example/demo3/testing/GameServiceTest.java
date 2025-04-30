package org.example.demo3.testing;

import org.example.demo3.model.player.Player;
import org.example.demo3.model.service.GameService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class GameServiceTest {

    private GameService gameService;

    @BeforeEach
    void setUp(){
        gameService = new GameService();
    }

    @Test
    void testNewGame(){
        gameService.newGame();

        assertNotNull(gameService.getP1());
        assertNotNull(gameService.getP2());
        assertNotNull(gameService.getBoard());
        assertEquals(1, gameService.getRound());
        assertEquals(gameService.getP1(), gameService.getCurrentPlayer());

    }
    @Test
    void testHands(){
        gameService.newGame();
        Player p1 = gameService.getP1();
        Player p2 = gameService.getP2();

        assertEquals(10, p1.getHand().size());
        assertEquals(10, p2.getHand().size());
        assertTrue(p1.getDeck().size() < 30);
        assertTrue(p1.getDeck().size() < 30);

    }
}
