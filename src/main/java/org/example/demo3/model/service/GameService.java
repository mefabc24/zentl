package org.example.demo3.model.service;

import javafx.application.Platform;
import org.example.demo3.event.Event;
import org.example.demo3.event.GameStateUpdateEve;
import org.example.demo3.event.PlayCardRequest;
import org.example.demo3.event.PlayerPassed;
import org.example.demo3.model.board.GameBoard;
import org.example.demo3.model.cards.Card;
import org.example.demo3.model.enums.BotDifficulty;
import org.example.demo3.model.enums.Faction;
import org.example.demo3.model.enums.GameMode;
import org.example.demo3.model.logic.PlayerFactory;
import org.example.demo3.model.player.AdvancedBot;
import org.example.demo3.model.player.EasyBot;
import org.example.demo3.network.Client;
import org.example.demo3.network.NetworkGson;
import org.example.demo3.network.NetworkMessage;
import org.example.demo3.network.Server;
import com.google.gson.JsonSyntaxException;

import java.util.ArrayList;
import java.util.List;

public class GameService extends AbstractGameService {
    private boolean isHost;
    private Server server;
    private Client client;

    public GameService() {
        super();
    }

    @Override
    public void newGame(GameMode mode, BotDifficulty difficulty, Faction p1Faction, Faction p2Faction) {
        this.isGameActive = true;
        this.gameMode = mode;
        this.botDifficulty = difficulty;

        this.board = new GameBoard();
        this.p1 = createPlayer("Player 1", p1Faction);

        List<Card> p2Deck = cardRepository.getSavedDeck(p2Faction);
        if (p2Deck.isEmpty()) {
            p2Deck = createCustomBotDeck();
        }
        this.p2 = PlayerFactory.createPlayer2(mode, difficulty, p2Faction, p2Deck);

        if (p1.getDeck().isEmpty() || p2.getDeck().isEmpty()) {
            endGame();
            return;
        }

        this.round = 1;
        this.currentPlayer = p1;
        dealInitialHands();
        postGameState();
    }

    public boolean isLanGame() {
        return this.server != null || this.client != null;
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

        if (currentPlayer instanceof AdvancedBot bot) {
            // prevent UI freeze while bot thinks
            new Thread(() -> {
                try {
                    Thread.sleep(1000);
                    if (!isGameActive) return;

                    Card cardToPlay = bot.chooseCardToPlay(p1, board, round);

                    // return to javafx thread with the card the bot wants to play
                    Platform.runLater(() -> {
                        if (!isGameActive) return;
                        if (cardToPlay != null) {
                            eventBus.post(new PlayCardRequest(cardToPlay));
                        } else {
                            eventBus.post(new PlayerPassed());
                        }
                    });
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }).start();

        } else if (currentPlayer instanceof EasyBot bot) {
            new Thread(() -> {
                try {
                    Thread.sleep(1000);
                    if (!isGameActive) return;

                    Card cardToPlay = ((EasyBot) currentPlayer).chooseCardToPlay();
                    Platform.runLater(() -> {
                        if (!isGameActive) return;
                        if (cardToPlay != null) {
                            eventBus.post(new PlayCardRequest(cardToPlay));
                        } else {
                            eventBus.post(new PlayerPassed());
                        }
                    });
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }).start();
        }
    }

    // FOR TESTING THE BOTS
    private List<Card> createCustomBotDeck() {
        List<Card> customDeck = new ArrayList<>();

        addCardByName(customDeck, "Paladin", 2);
        addCardByName(customDeck, "Pikeman", 2);
        addCardByName(customDeck, "Knight Errant", 2);
        addCardByName(customDeck, "Ballista Crew", 2);

        addCardByName(customDeck, "Heavy Cavalry", 2);
        addCardByName(customDeck, "Crossbowman", 2);
        addCardByName(customDeck, "Field Medic", 1);

        addCardByName(customDeck, "Commander's Horn", 1);
        addCardByName(customDeck, "Scorch", 1);
        addCardByName(customDeck, "Biting Frost", 1);
        addCardByName(customDeck, "Clear Skies", 1);

        return customDeck;
    }

    private void addCardByName(List<Card> deck, String name, int count) {
        cardRepository.getAllCards().stream()
                .filter(c -> c.getName().equalsIgnoreCase(name))
                .findFirst()
                .ifPresent(card -> {
                    for (int i = 0; i < count; i++) {
                        deck.add(card);
                    }
                });
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
        GameStateUpdateEve eve = new GameStateUpdateEve(p1, p2, currentPlayer, round, board, gameMode, botDifficulty);

        // (HOST) send the new game state to the client => this is the main synchronization mechanism
        if (isHost && server != null) {
            String json = NetworkGson.getInstance().toJson(eve);
            NetworkMessage msg = new NetworkMessage(eve.getClass().getSimpleName(), json);
            server.sendMessage(NetworkGson.getInstance().toJson(msg));
        }

        // everyone posts the event to their local eventbus to update their own UI
        eventBus.post(eve);
    }

    // configures game service to act as host
    public void startLanGameAsHost(Server server) {
        this.isHost = true;
        this.server = server;
        // re-assign the message handler to this classes method
        this.server.setOnMessageReceived(this::handleClientMessage);
    }

    // configures the game service to act as a client
    public void startLanGameAsClient(Client client) {
        this.isHost = false;
        this.client = client;
        this.client.setOnMessageReceived(this::handleServerMessage);
    }

    // (HOST) processes incoming messages from the client
    private void handleClientMessage(String jsonMessage) {
        try {
            NetworkMessage msg = NetworkGson.getInstance().fromJson(jsonMessage, NetworkMessage.class);
            String eventType = msg.getEventType();
            String jsonData = msg.getJsonData();

            if (eventType.equals(PlayCardRequest.class.getSimpleName())) {
                PlayCardRequest event = NetworkGson.getInstance().fromJson(jsonData, PlayCardRequest.class);
                super.cardPlayed(event.getCard());
            } else if (eventType.equals(PlayerPassed.class.getSimpleName())) {
                super.playerPassed();
            }
        } catch (JsonSyntaxException e) {
            System.err.println("rrror deserializing client message: " + jsonMessage);
        }
    }

    // CLIENT) processes incoming messages from the server
    private void handleServerMessage(String jsonMessage) {
        try {
            NetworkMessage msg = NetworkGson.getInstance().fromJson(jsonMessage, NetworkMessage.class);
            String eventType = msg.getEventType();
            String jsonData = msg.getJsonData();

            // the client only receives state updates and posts them to its local event bus for the UI
            Platform.runLater(() -> {
                if (eventType.equals(GameStateUpdateEve.class.getSimpleName())) {
                    GameStateUpdateEve event = NetworkGson.getInstance().fromJson(jsonData, GameStateUpdateEve.class);
                    eventBus.post(event);
                }
            });

        } catch (JsonSyntaxException e) {
            System.err.println("error deserializing server message: " + jsonMessage);
        }
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
