package org.example.demo3.service;

import org.example.demo3.event.*;
import org.example.demo3.model.board.*;
import org.example.demo3.model.cards.*;
import org.example.demo3.model.enums.*;
import org.example.demo3.logic.*;
import org.example.demo3.model.player.*;

import java.util.*;

public class GameServiceImpl implements GameService {

    private GameBoard gameBoardState;
    private Player player1;
    private Player player2;
    private Player currentPlayer;
    private int round = 1;
    private int player1Wins = 0;
    private int player2Wins = 0;
    private Player roundWinner;
    private final EventBus eventBus = EventBus.getInstance();
    private final GameEngine gameEngine;

    public GameServiceImpl() {
        this.gameEngine = new GameEngineImpl();
    }

    @Override
    public void initializeGame() {
        player1Wins = 0;
        player2Wins = 0;
        round = 1;
        gameBoardState = new GameBoardImpl();

        List<Card> deck1 = createStarterDeck(Faction.NORTHERN_REALMS);
        List<Card> deck2 = createStarterDeck(Faction.MONSTERS);

        player1 = new PlayerImpl("Player 1", Faction.NORTHERN_REALMS, deck1);
        player2 = new PlayerImpl("Player 2", Faction.MONSTERS, deck2);

        for (int i = 0; i < 10; i++) {
            player1.drawCard();
            player2.drawCard();
        }

        currentPlayer = player1;
        eventBus.post(new InitEve(player1, player2));
    }


    private List<Card> createStarterDeck(Faction fraction) {
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
        deck.add(new SpecialCard("Clear Weather", "Removes all weather effects", fraction));
        deck.add(new SpecialCard("Scorch", "Destroys the strongest unit(s)", fraction));
        Collections.shuffle(deck);
        return deck;
    }

    @Override
    public void playCard(Player player, Card card) {
        if (player != currentPlayer || player.hasPassed()) {
            return;
        }
        player.playCard(card, gameBoardState);
        eventBus.post(new CardPlayedEve(player, card));
        switchPlayer();
    }

    @Override
    public void playerPass(Player player) {
        if (player != currentPlayer || player.hasPassed()) {
            return;
        }
        player.pass();
        eventBus.post(new PlayerPassedEve(player));
        switchPlayer();
    }

    private void switchPlayer() {
        if (player1.hasPassed() && player2.hasPassed()) {
            endRound();
        } else {
            Player nextPlayer = (currentPlayer == player1) ? player2 : player1;
            if (!nextPlayer.hasPassed()) {
                currentPlayer = nextPlayer;
                eventBus.post(new PlayerChangedEve(currentPlayer));
            }
        }
    }

    private void endRound() {
        int p1Score = getPlayerScore(player1);
        int p2Score = getPlayerScore(player2);

         roundWinner = gameEngine.determineRoundWinner(player1, player2, p1Score, p2Score);

        if (roundWinner == player1) {
            player1Wins++;
        } else if (roundWinner == player2) {
            player2Wins++;
        } else {
            player1Wins++;
            player2Wins++;
        }
        eventBus.post(new RoundEndedEve(roundWinner, p1Score, p2Score));

        int BO = 2;
        if (player1Wins >= BO || player2Wins >= BO) {
            endGame();
        } else {
            startNewRound();
        }
    }

    private void endGame() {
        Player gameWinner = gameEngine.determineGameWinner(player1, player2, player1Wins, player2Wins);
        System.out.println("Spiel beendet. Gesamtsieger: " + (gameWinner != null ? gameWinner.getName() : "Unentschieden"));
        eventBus.post(new GameEndedEve(gameWinner, player1Wins, player2Wins));
    }

    @Override
    public void startNewRound() {
        round++;
        gameBoardState.clearBoard();
        player1.resetPass();
        player2.resetPass();

        for (int i = 0; i < 3; i++) {
            if (!player1.getDeck().isEmpty()) player1.drawCard();
            if (!player2.getDeck().isEmpty()) player2.drawCard();
        }

        currentPlayer = roundWinner == player1 ? player1 : player2;
        eventBus.post(new InitEve(player1, player2));
    }

    @Override
    public void restartGame() {
        initializeGame();
    }

    @Override
    public Player getCurrentPlayer() {
        return currentPlayer;
    }

    @Override
    public int getRound() {
        return round;
    }

    @Override
    public int getPlayerScore(Player player) {
        return gameBoardState.calculateTotalPower(player);
    }

    @Override
    public boolean isRoundOver() {
        return player1.hasPassed() && player2.hasPassed();
    }

    @Override
    public GameBoard getGameBoard() {
        return gameBoardState;
    }

    @Override
    public Player getPlayer1() {
        return player1;
    }

    @Override
    public Player getPlayer2() {
        return player2;
    }

    @Override
    public int getPlayer1Wins() {
        return player1Wins;
    }

    @Override
    public int getPlayer2Wins() {
        return player2Wins;
    }
}
