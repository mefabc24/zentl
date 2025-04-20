package org.example.demo3.model.service;

import org.example.demo3.event.*;
import org.example.demo3.model.logic.Engine;
import org.example.demo3.model.logic.GameEngine;
import org.example.demo3.model.board.GameBoard;
import org.example.demo3.model.cards.Card;
import org.example.demo3.model.cards.UnitCard;
import org.example.demo3.model.enums.Fraction;
import org.example.demo3.model.enums.RowType;
import org.example.demo3.model.player.Player;
import org.example.demo3.model.player.PlayerImpl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GameService implements Service {
    private static final int BEST_OF = 3;
    private final EventBus eventBus = EventBus.getInstance();
    private final Engine gameEngine = new GameEngine();
    private GameBoard board;
    private Player p1;
    private Player p2;
    private Player currentPlayer;
    private int round;
    private Player roundWinner;

    public GameService() {
        subscribeToEvents();
    }

    private void subscribeToEvents() {
        eventBus.subscribe(RestartEve.class, restartEve -> newGame());
        eventBus.subscribe(PlayCardRequest.class, this::CardPlayed);
        eventBus.subscribe(PlayerPassed.class, this::PlayerPassed);
    }

    public void newGame() {
        board = new GameBoard();
        p1 = createPlayer("Player 1", Fraction.KNIGHTS);
        p2 = createPlayer("Player 2", Fraction.MONSTER);
        round = 1;
        roundWinner = null;
        dealInitialHands();
        currentPlayer = p1;
        postGameState();
    }

    private Player createPlayer(String name, Fraction fraction) {
        List<Card> deck = generateDeck(fraction);
        Player player = new PlayerImpl(name, fraction, deck);
        player.setWins(0);
        return player;
    }

    private void dealInitialHands() {
        for (int i = 0; i < 10; i++) {
            p1.drawCard();
            p2.drawCard();
        }
    }

    private List<Card> generateDeck(Fraction fraction) {
        List<Card> deck = new ArrayList<>();
        for (int i = 0; i < 15; i++)
            deck.add(new UnitCard("Soldier " + (i + 1), 5, "Basic soldier", fraction, RowType.MELEE));
        for (int i = 0; i < 10; i++)
            deck.add(new UnitCard("Archer " + (i + 1), 4, "Basic archer", fraction, RowType.RANGED));
        for (int i = 0; i < 5; i++)
            deck.add(new UnitCard("Catapult " + (i + 1), 6, "Basic siege", fraction, RowType.SIEGE));
        Collections.shuffle(deck);
        return deck;
    }

    private void CardPlayed(PlayCardRequest event) {
        event.getPlayer().playCard(event.getCard(), board);
        nextTurn();
    }

    private void PlayerPassed(PlayerPassed event) {
        event.getPlayer().pass();
        nextTurn();
    }

    private void nextTurn() {
        if (p1.hasPassed() && p2.hasPassed()) {
            finishRound();
        } else {
            Player next = (currentPlayer == p1) ? p2 : p1;
            if (!next.hasPassed()) {
                currentPlayer = next;
            }
            postGameState();
        }
    }

    private void finishRound() {
        updateScores();
        roundWinner = gameEngine.determineRoundWinner(p1, p2);
        if (roundWinner == p1) p1.setWins(p1.getWins() + 1);
        else if (roundWinner == p2) p2.setWins(p2.getWins() + 1);
        else {
            p1.setWins(p1.getWins() + 1);
            p2.setWins(p2.getWins() + 1);
        }
        eventBus.post(new RoundEndedEve(round, roundWinner, p1, p2));
        if (p1.getWins() < BEST_OF - 1 && p2.getWins() < BEST_OF - 1) {
            prepareNextRound();
        } else {
            endGame();
        }
    }

    private void prepareNextRound() {
        round++;
        board.clearBoard();
        p1.resetPass();
        p2.resetPass();
        p1.setScore(0);
        p2.setScore(0);
        for (int i = 0; i < 3; i++) {
            if (!p1.getDeck().isEmpty()) p1.drawCard();
            if (!p2.getDeck().isEmpty()) p2.drawCard();
        }
        currentPlayer = (roundWinner == p2) ? p2 : p1;
        postGameState();
    }

    private void endGame() {
        Player winner = gameEngine.determineGameWinner(p1, p2);
        eventBus.post(new GameEndedEve(winner, p1.getWins(), p2.getWins(), p1.getName(), p2.getName()));
    }

    private void postGameState() {
        updateScores();
        eventBus.post(new GameStateUpdateEve(p1, p2, currentPlayer, round, board));
    }

    private void updateScores() {
        p1.setScore(board.calculateTotalPower(p1));
        p2.setScore(board.calculateTotalPower(p2));
    }
}