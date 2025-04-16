package org.example.demo3.service;

import org.example.demo3.event.*;
import org.example.demo3.model.*;
import org.example.demo3.model.enums.*;
import java.util.*;
import java.util.function.Consumer;

// Hier nur Spiellogik und Spielzustand, man könnte Spiellogik noch separieren in extra Klasse GameEngine
public class GameServiceImpl implements GameService {
    private GameBoard gameBoardState;
    private Player player1;
    private Player player2;
    private Player currentPlayer;
    private int round = 1;
    private int player1Wins = 0;
    private int player2Wins = 0;
    private final int MAX_ROUNDS = 3;

    // EventBus statt direkter Listener-Liste verwenden
    private final EventBus eventBus = EventBus.getInstance();

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

        // Event posten statt Listener direkt zu benachrichtigen
        eventBus.post(new InitEvent(player1, player2));
    }

    private List<Card> createStarterDeck(Faction faction) {
        List<Card> deck = new ArrayList<>();
        for (int i = 0; i < 15; i++) {
            deck.add(new UnitCard("Soldier " + (i + 1), 5, "Basic soldier", faction, RowType.MELEE));
        }
        for (int i = 0; i < 10; i++) {
            deck.add(new UnitCard("Archer " + (i + 1), 4, "Basic archer", faction, RowType.RANGED));
        }
        for (int i = 0; i < 5; i++) {
            deck.add(new UnitCard("Catapult " + (i + 1), 6, "Basic siege engine", faction, RowType.SIEGE));
        }

        deck.add(new SpecialCard("Clear Weather", "Removes all weather effects", faction));
        deck.add(new SpecialCard("Scorch", "Destroys the strongest unit(s)", faction));
        return deck;
    }

    @Override
    public void playCard(Player player, Card card) {
        if (player != currentPlayer || player.hasPassed()) {
            return;
        }

        player.playCard(card, gameBoardState);

        // Event posten statt Listener direkt zu benachrichtigen
        eventBus.post(new CardPlayedEvent(player, card));

        switchPlayer();
    }

    @Override
    public void playerPass(Player player) {
        if (player != currentPlayer || player.hasPassed()) {
            return;
        }

        player.pass();

        // Event posten statt Listener direkt zu benachrichtigen
        eventBus.post(new PlayerPassedEvent(player));

        switchPlayer();
    }

    private void switchPlayer() {
        if (player1.hasPassed() && player2.hasPassed()) {
            endRound();
        } else {
            currentPlayer = (currentPlayer == player1) ? player2 : player1;

            // Event posten statt Listener direkt zu benachrichtigen
            eventBus.post(new PlayerChangedEvent(currentPlayer));
        }
    }

    private void endRound() {
        int p1Score = gameBoardState.calculateTotalPower(player1);
        int p2Score = gameBoardState.calculateTotalPower(player2);

        Player roundWinner;
        if (p1Score > p2Score) {
            player1Wins++;
            roundWinner = player1;
        } else if (p2Score > p1Score) {
            player2Wins++;
            roundWinner = player2;
        } else {
            roundWinner = null;
        }

        // Event posten statt Listener direkt zu benachrichtigen
        eventBus.post(new RoundEndedEvent(roundWinner, p1Score, p2Score));

        if (round >= MAX_ROUNDS) {
            endGame();
        }
    }

    private void endGame() {
        Player gameWinner;
        if (player1Wins > player2Wins) {
            gameWinner = player1;
        } else if (player2Wins > player1Wins) {
            gameWinner = player2;
        } else {
            gameWinner = null;
        }

        // Event posten statt Listener direkt zu benachrichtigen
        eventBus.post(new GameEndedEvent(gameWinner, player1Wins, player2Wins));
    }

    @Override
    public void startNewRound() {
        if (round >= MAX_ROUNDS) {
            endGame();
            return;
        }

        round++;
        gameBoardState.clearBoard();

        List<Card> p1AllCards = new ArrayList<>();
        p1AllCards.addAll(player1.getHand());
        p1AllCards.addAll(player1.getDeck());

        List<Card> p2AllCards = new ArrayList<>();
        p2AllCards.addAll(player2.getHand());
        p2AllCards.addAll(player2.getDeck());

        player1 = new PlayerImpl(player1.getName(), player1.getFaction(), p1AllCards);
        player2 = new PlayerImpl(player2.getName(), player2.getFaction(), p2AllCards);

        player1.resetPass();
        player2.resetPass();

        for (int i = 0; i < 10; i++) {
            player1.drawCard();
            player2.drawCard();
        }

        currentPlayer = player1;

        // Event posten statt Listener direkt zu benachrichtigen
        eventBus.post(new InitEvent(player1, player2));
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

    // Neue Methode zur flexiblen Event-Handler-Registrierung
    @Override
    public <T extends Event> void registerEventHandler(Class<T> eventType, Consumer<T> handler) {
        eventBus.subscribe(eventType, handler);
    }

    public GameBoard getGameBoard() {
        return gameBoardState;
    }

    public Player getPlayer1() {
        return player1;
    }

    public Player getPlayer2() {
        return player2;
    }

    public int getPlayer1Wins() {
        return player1Wins;
    }

    public int getPlayer2Wins() {
        return player2Wins;
    }
}
