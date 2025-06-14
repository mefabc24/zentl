package org.example.demo3.model.service;

import com.google.gson.JsonSyntaxException;
import javafx.application.Platform;
import org.example.demo3.event.*;
import org.example.demo3.model.board.GameBoard;
import org.example.demo3.model.cards.Card;
import org.example.demo3.model.enums.BotDifficulty;
import org.example.demo3.model.enums.Faction;
import org.example.demo3.model.enums.GameMode;
import org.example.demo3.model.logic.PlayerFactory;
import org.example.demo3.model.player.AdvancedBot;
import org.example.demo3.model.player.Player;
import org.example.demo3.model.player.TacticalBot;
import org.example.demo3.network.Client;
import org.example.demo3.network.NetworkGson;
import org.example.demo3.network.NetworkMessage;
import org.example.demo3.network.Server;

import java.util.List;

public class GameService extends AbstractGameService {
    private boolean isHost;
    private Server server;
    private Client client;

    public GameService() {
        super();
    }

    // This is an overloaded method for convenience, it calls the main one
    public void newGame(GameMode mode, BotDifficulty difficulty, Faction p1Faction, Faction p2Faction) {
        this.newGame(mode, difficulty, BotDifficulty.NONE, p1Faction, p2Faction);
    }

    // This is the main method that handles all game modes, including LAN setup
    public void newGame(GameMode mode, BotDifficulty bot1Difficulty, BotDifficulty bot2Difficulty, Faction p1Faction, Faction p2Faction) {
        this.isGameActive = true;
        this.gameMode = mode;
        this.bot1Difficulty = bot1Difficulty;
        this.bot2Difficulty = bot2Difficulty;

        this.board = new GameBoard();

        // Create Player 1
        if (mode == GameMode.BOT_VS_BOT) {
            List<Card> p1Deck = cardRepository.getRandomDeck(p1Faction);
            this.p1 = PlayerFactory.createPlayer("Bot 1", bot1Difficulty, p1Faction, p1Deck);
        } else {
            // For PvP, PvB, and LAN, Player 1 is human
            this.p1 = createPlayer("Player 1", p1Faction);
        }

        // Create Player 2
        if (mode == GameMode.BOT_VS_BOT) {
            List<Card> p2Deck = cardRepository.getRandomDeck(p2Faction);
            this.p2 = PlayerFactory.createPlayer("Bot 2", bot2Difficulty, p2Faction, p2Deck);
        } else {
            // For PvP, PvB, and LAN, Player 2 is created based on mode
            List<Card> p2Deck = cardRepository.getSavedDeck(p2Faction);
            if (p2Deck.isEmpty()) {
                p2Deck = cardRepository.getRandomDeck(p2Faction);
            }
            this.p2 = PlayerFactory.createPlayer2(mode, bot1Difficulty, p2Faction, p2Deck);
        }

        if (p1.getDeck().isEmpty() || p2.getDeck().isEmpty()) {
            endGame();
            return;
        }

        this.round = 1;
        this.currentPlayer = p1;
        dealInitialHands();
        postGameState();

        // If the very first player is a bot (in BvB mode), kick off its turn.
        if (!isLanGame() && (currentPlayer instanceof TacticalBot || currentPlayer instanceof AdvancedBot)) {
            handleBotTurn();
        }
    }

    @Override
    protected void nextTurn() {
        if (!isGameActive) return;

        if (p1.hasPassed() && p2.hasPassed()) {
            finishRound();
            return;
        }

        currentPlayer = (currentPlayer == p1) ? p2 : p1;
        if (currentPlayer.hasPassed()) {
            currentPlayer = (currentPlayer == p1) ? p2 : p1;
        }

        postGameState();

        // Handle bot turn logic only for local games
        if (!isLanGame() && (currentPlayer instanceof TacticalBot || currentPlayer instanceof AdvancedBot)) {
            handleBotTurn();
        }
    }

    private void handleBotTurn() {
        new Thread(() -> {
            try {
                Thread.sleep(1000); // sleep
                if (!isGameActive) return;

                Card cardToPlay = null;
                if (currentPlayer instanceof AdvancedBot bot) {
                    Player opponent = (currentPlayer == p1) ? p2 : p1;
                    cardToPlay = bot.chooseCardToPlay(opponent, board, round);
                    System.out.println("Advanced Bot plays: " + cardToPlay);
                } else if (currentPlayer instanceof TacticalBot bot) {
                    Player opponent = (currentPlayer == p1) ? p2 : p1;
                    cardToPlay = bot.chooseCardToPlay(opponent, board, round);
                    System.out.println("Tactical Bot plays: " + cardToPlay);
                }

                final Card finalCardToPlay = cardToPlay;
                Platform.runLater(() -> {
                    if (!isGameActive) return;
                    if (finalCardToPlay != null) {
                        eventBus.post(
                                new PlayCardRequest(finalCardToPlay)
                        );
                    } else {
                        eventBus.post(new PlayerPassed());
                    }
                });
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }

    @Override
    protected void cardPlayed(Card card) {
        if (!isGameActive) return;

        // (LAN)
        if (isLanGame()) {
            if (isHost) {
                super.cardPlayed(card);
            } else {
                sendActionToServer(new PlayCardRequest(card));
            }
        } else { // local game
            super.cardPlayed(card);
        }
    }


    @Override
    protected void playerPassed() {
        if (!isGameActive) return;

        if (isLanGame()) {
            if (isHost) {
                super.playerPassed();
            } else {
                sendActionToServer(new PlayerPassed());
            }
        } else {
            super.playerPassed();
        }
    }


    // (CLIENT) serialize event and send it to server
    private void sendActionToServer(Event action) {
        String eventType = action.getClass().getSimpleName();
        String json = NetworkGson.getInstance().toJson(action);
        NetworkMessage msg = new NetworkMessage(eventType, json);
        client.sendMessage(NetworkGson.getInstance().toJson(msg));
    }

    // posts the current game state to the event bus and network
    @Override
    protected void postGameState() {
        if (!isGameActive) return;
        updateScores();
        GameStateUpdateEve eve = new GameStateUpdateEve(p1, p2, currentPlayer, round, board, gameMode, bot1Difficulty, bot2Difficulty);

        // (HOST) send the new game state to the client
        if (isHost && server != null) {
            String json = NetworkGson.getInstance().toJson(eve);
            NetworkMessage msg = new NetworkMessage(eve.getClass().getSimpleName(), json);
            server.sendMessage(NetworkGson.getInstance().toJson(msg));
        }

        // everyone posts the event to their local eventbus to update their own UI
        eventBus.post(eve);
    }

    public boolean isLanGame() {
        return this.server != null || this.client != null;
    }

    // configures game service to act as host
    public void startLanGameAsHost(Server server) {
        this.isHost = true;
        this.server = server;
        this.server.setOnMessageReceived(this::handleClientMessage);
        this.server.setOnConnectionFailed(this::handleDisconnection);
    }

    // configures the game service to act as a client
    public void startLanGameAsClient(Client client) {
        this.isHost = false;
        this.client = client;
        this.client.setOnMessageReceived(this::handleServerMessage);
        this.client.setOnConnectionFailed(this::handleDisconnection);
    }

    // sends a disconnect notice to the other player in a LAN game.
    public void notifyDisconnection() {
        if (!isLanGame()) return;

        final String disconnectMessage = "DISCONNECT_NOTICE";
        if (isHost) {
            if (server != null) server.sendMessage(disconnectMessage);
        } else {
            if (client != null) client.sendMessage(disconnectMessage);
        }

        // short pause to make sure the msg is send
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    // (HOST) processes incoming messages from the client
    private void handleClientMessage(String jsonMessage) {

        if ("DISCONNECT_NOTICE".equals(jsonMessage)) {
            handleDisconnection();
            return;
        }

        try {
            NetworkMessage msg = NetworkGson
                    .getInstance()
                    .fromJson(jsonMessage, NetworkMessage.class);
            String eventType = msg.getEventType();
            String jsonData = msg.getJsonData();

            Platform.runLater(() -> {
                if (eventType.equals(PlayCardRequest.class.getSimpleName())) {
                    PlayCardRequest event = NetworkGson
                            .getInstance()
                            .fromJson(jsonData, PlayCardRequest.class);
                    // Host executes the action on behalf of Player 2
                    super.cardPlayed(event.getCard());
                } else if (eventType.equals(PlayerPassed.class.getSimpleName())) {
                    // Host executes the action on behalf of Player 2
                    super.playerPassed();
                }
            });
        } catch (JsonSyntaxException e) {
            System.err.println(
                    "Error deserializing client message: " + jsonMessage
            );
        }
    }

    // (CLIENT) processes incoming messages from the server
    private void handleServerMessage(String jsonMessage) {
        if ("DISCONNECT_NOTICE".equals(jsonMessage)) {
            handleDisconnection();
            return;
        }
        try {
            NetworkMessage msg = NetworkGson
                    .getInstance()
                    .fromJson(jsonMessage, NetworkMessage.class);
            String eventType = msg.getEventType();
            String jsonData = msg.getJsonData();

            // The client only receives state updates and posts them to its local event bus
            Platform.runLater(() -> {
                if (eventType.equals(GameStateUpdateEve.class.getSimpleName())) {
                    GameStateUpdateEve event = NetworkGson
                            .getInstance()
                            .fromJson(jsonData, GameStateUpdateEve.class);
                    eventBus.post(event);
                }
            });
        } catch (JsonSyntaxException e) {
            System.err.println(
                    "Error deserializing server message: " + jsonMessage
            );
        }
    }

    // inform about disconnect to the other player
    private void handleDisconnection() {
        if (!isGameActive) return;
        isGameActive = false;

        String message = isHost ? "The client has disconnected." : "Connection to the host was lost.";

        eventBus.post(new PlayerDisconnectedEvent(message));

        shutdownNetwork();
    }

    // shuts down the network components
    public void shutdownNetwork() {
        if (server != null) {
            server.stop();
        }
        if (client != null) {
            client.stop();
        }
    }
}