package org.example.demo3.service;

import org.example.demo3.event.*;
import org.example.demo3.logic.GameEngine;
import org.example.demo3.logic.GameEngineImpl;
import org.example.demo3.model.board.GameBoard;
import org.example.demo3.model.board.GameBoardImpl;
import org.example.demo3.model.cards.Card;
import org.example.demo3.model.cards.UnitCard;
import org.example.demo3.model.enums.Fraction;
import org.example.demo3.model.enums.RowType;
import org.example.demo3.model.player.Player;
import org.example.demo3.model.player.PlayerImpl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GameServiceImpl implements GameService {
    private final EventBus eventBus = EventBus.getInstance();
    private final GameEngine gameEngine;
    private GameBoard board;
    private Player p1;
    private Player p2;
    private Player currentPlayer;
    private int round = 1;
    private Player roundWinner;

    public GameServiceImpl() {
        this.gameEngine = new GameEngineImpl();
        registerEventHandlers();
    }

    private void registerEventHandlers() {
        eventBus.subscribe(RestartEve.class, restartEve -> initializeGame());
        eventBus.subscribe(PlayCardCommand.class, this::handlePlayCardCommand);
        eventBus.subscribe(PassCommand.class, this::handlePassCommand);
    }

    public void initializeGame() {
        round = 1;
        board = new GameBoardImpl();
        List<Card> deck1 = createStarterDeck(Fraction.KNIGHTS);
        List<Card> deck2 = createStarterDeck(Fraction.MONSTER);
        p1 = new PlayerImpl("Player 1", Fraction.KNIGHTS, deck1);
        p2 = new PlayerImpl("Player 2", Fraction.MONSTER, deck2);
        p1.setWins(0);
        p2.setWins(0);
        for (int i = 0; i < 10; i++) {
            p1.drawCard();
            p2.drawCard();
        }
        currentPlayer = p1;
        roundWinner = null;
        updateAndPostState();
    }

    private List<Card> createStarterDeck(Fraction fraction) {
        List<Card> deck = new ArrayList<>();
        for (int i = 0; i < 15; i++) {
            deck.add(new UnitCard("Soldier " + (i + 1), 5, "Basic soldier", fraction, RowType.MELEE));
        }
        for (int i = 0; i < 10; i++) {
            deck.add(new UnitCard("Archer " + (i + 1), 4, "Basic archer", fraction, RowType.RANGED));
        }
        for (int i = 0; i < 5; i++) {
            deck.add(new UnitCard("Catapult " + (i + 1), 6, "Basic siege", fraction, RowType.SIEGE));
        }
        Collections.shuffle(deck);
        return deck;
    }

    private void handlePlayCardCommand(PlayCardCommand command) {
        playCard(command.getPlayer(), command.getCard());
    }

    private void handlePassCommand(PassCommand command) {
        playerPass(command.getPlayer());
    }

    public void playCard(Player player, Card card) {
        if (player != currentPlayer || player.hasPassed()) {
            System.out.println("Invalid move: Not player's turn or player has passed.");
            return;
        }
        if (!player.getHand().contains(card)) {
            System.out.println("Invalid move: Player does not have card " + card.getName());
            return;
        }
        player.playCard(card, board);
        switchPlayer();
        updateAndPostState();
    }

    public void playerPass(Player player) {
        if (player != currentPlayer || player.hasPassed()) {
            System.out.println("Invalid pass: Not player's turn or player already passed.");
            return;
        }
        player.pass();
        switchPlayer();
        updateAndPostState();
    }

    private void switchPlayer() {
        if (p1.hasPassed() && p2.hasPassed()) {
            endRound();
        } else {
            Player nextPlayer = (currentPlayer == p1) ? p2 : p1;
            if (!nextPlayer.hasPassed()) {
                currentPlayer = nextPlayer;
            }
        }
    }

    private void endRound() {
        int p1Score = board.calculateTotalPower(p1);
        int p2Score = board.calculateTotalPower(p2);
        p1.setScore(p1Score);
        p2.setScore(p2Score);
        roundWinner = gameEngine.determineRoundWinner(p1, p2);
        int currentP1Wins = p1.getWins();
        int currentP2Wins = p2.getWins();
        if (roundWinner == p1) {
            p1.setWins(currentP1Wins + 1);
        } else if (roundWinner == p2) {
            p2.setWins(currentP2Wins + 1);
        } else {
            p1.setWins(currentP1Wins + 1);
            p2.setWins(currentP2Wins + 1);
        }
        eventBus.post(new RoundEndedEve(round, roundWinner, p1, p2));
        int BO = 2;
        if (p1.getWins() >= BO || p2.getWins() >= BO) {
            endGame();
        } else {
            startNewRound();
        }
    }

    private void endGame() {
        Player gameWinner = gameEngine.determineGameWinner(p1, p2);
        System.out.println("Spiel beendet. Gesamtsieger: " + (gameWinner != null ? gameWinner.getName() : "Unentschieden"));
        eventBus.post(new GameEndedEve(gameWinner, p1.getWins(), p2.getWins(), p1.getName(), p2.getName()));
    }

    public void startNewRound() {
        round++;
        board.clearBoard();
        p1.resetPass();
        p2.resetPass();
        p1.setScore(0);
        p2.setScore(0);
        for (int i = 0; i < 2; i++) {
            if (!p1.getDeck().isEmpty()) p1.drawCard();
            if (!p2.getDeck().isEmpty()) p2.drawCard();
        }
        currentPlayer = (roundWinner == p2) ? p2 : p1;
        updateAndPostState();
    }

    private void updateAndPostState() {
        int currentP1Score = board.calculateTotalPower(p1);
        int currentP2Score = board.calculateTotalPower(p2);
        p1.setScore(currentP1Score);
        p2.setScore(currentP2Score);
        GameStateUpdateEve stateEvent = new GameStateUpdateEve(p1, p2, currentPlayer, round, board);
        eventBus.post(stateEvent);
    }

    public Player getCurrentPlayer() {
        return currentPlayer;
    }

    public int getRound() {
        return round;
    }

    public GameBoard getGameBoard() {
        return board;
    }

    public Player getPlayer1() {
        return p1;
    }

    public Player getPlayer2() {
        return p2;
    }
}