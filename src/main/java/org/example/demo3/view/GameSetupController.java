package org.example.demo3.view;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import org.example.demo3.model.cards.Card;
import org.example.demo3.model.enums.BotDifficulty;
import org.example.demo3.model.enums.Faction;
import org.example.demo3.model.enums.GameMode;
import org.example.demo3.model.logic.CardRepository;
import org.example.demo3.model.service.GameService;
import org.example.demo3.model.service.NavigationService;
import org.example.demo3.model.service.SoundService;
import org.example.demo3.network.Client;
import org.example.demo3.network.Server;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.example.demo3.model.constants.Config.*;

//  game setup screen => handle faction selection for LAN or HotSeat/Bot Mode
public class GameSetupController {

    @FXML private ComboBox<Faction> p1FactionComboBox;
    @FXML private Label p1DeckStatusLabel;
    @FXML private ComboBox<Faction> p2FactionComboBox;
    @FXML private Label p2DeckStatusLabel;
    @FXML private Button startGameButton;
    @FXML private Button backButton;

    private NavigationService navigationService;
    private CardRepository cardRepository;
    private GameMode gameMode;
    private BotDifficulty botDifficulty;

    private boolean isHost; // flag to determine if this player is the host or client
    private Server server; // host
    private Client client;

    public void setNavigationService(NavigationService navigationService) {
        this.navigationService = navigationService;
    }

    @FXML
    public void initialize() {
        cardRepository = CardRepository.getInstance();

        List<Faction> playableFactions = Stream.of(Faction.values())
                .filter(f -> f != Faction.TEST)
                .collect(Collectors.toList());

        p1FactionComboBox.setItems(FXCollections.observableArrayList(playableFactions));
        p2FactionComboBox.setItems(FXCollections.observableArrayList(playableFactions));

        p1FactionComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (!isLanGame()) {
                updateDeckStatusLabels();
            }
        });
        p2FactionComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (!isLanGame()) {
                updateDeckStatusLabels();
            }
        });

        p1FactionComboBox.setValue(Faction.KNIGHTS);
        p2FactionComboBox.setValue(Faction.MONSTERS);

        updateDeckStatusLabels();
    }

    public void initData(GameMode mode, BotDifficulty difficulty) {
        this.gameMode = mode;
        this.botDifficulty = difficulty;

        if (mode == GameMode.PLAYER_VS_BOT) {
            p2FactionComboBox.setDisable(true);
            p2FactionComboBox.setValue(Faction.MONSTERS);
            p2DeckStatusLabel.setText("Bot benutzt ein random Deck");
        }
    }

    // initializes the view for a lan game, configures UI based on if the player is host or client
    public void initLanData(NavigationService navigationService, boolean isHost, Server server, Client client) {
        setNavigationService(navigationService);
        this.isHost = isHost;
        this.server = server;
        this.client = client;
        this.gameMode = GameMode.PLAYER_VS_PLAYER;

        // setup host
        if (isHost) {
            p1FactionComboBox.setDisable(false);
            p2FactionComboBox.setDisable(true);
            startGameButton.setDisable(true);
            p1DeckStatusLabel.setText("Wähle deine Fraktion (Du bist Spieler 1)");
            p2DeckStatusLabel.setText("Warte auf Gegner-Auswahl...");

            // add a listener to send the faction to client
            p1FactionComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null) {
                    sendFactionChoice(newVal);
                    updateDeckStatusLabels();
                    checkIfBothFactionsSelected();
                }
            });
            // set the message handler for incoming client messages
            this.server.setOnMessageReceived(this::handleNetworkMessage);
        } else {
            // setup client
            p1FactionComboBox.setDisable(true);
            p2FactionComboBox.setDisable(false);
            startGameButton.setDisable(true);
            p1DeckStatusLabel.setText("Warte auf Host-Auswahl...");
            p2DeckStatusLabel.setText("Wähle deine Fraktion (Du bist Spieler 2)");

            // listener to send the faction choice to host
            p2FactionComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null) {
                    sendFactionChoice(newVal);
                    updateDeckStatusLabels();
                }
            });
            // set the message handler for incoming server messages
            this.client.setOnMessageReceived(this::handleNetworkMessage);
        }
    }

    // sends the chosen faction over the network
    private void sendFactionChoice(Faction faction) {
        if (isHost && server != null) {
            server.sendMessage("HOST_FACTION:" + faction.name());
        } else if (!isHost && client != null) {
            client.sendMessage("CLIENT_FACTION:" + faction.name());
        }
    }

    // processes incoming messages from the network
    private void handleNetworkMessage(String message) {
        Platform.runLater(() -> {
            // update the opponent faction
            if (message.startsWith("HOST_FACTION:")) {
                String factionName = message.substring("HOST_FACTION:".length());
                    Faction hostFaction = Faction.valueOf(factionName);
                    p1FactionComboBox.setValue(hostFaction);
                    updateDeckStatusLabels();
                    checkIfBothFactionsSelected();
            } else if (message.startsWith("CLIENT_FACTION:")) {
                String factionName = message.substring("CLIENT_FACTION:".length());
                    Faction clientFaction = Faction.valueOf(factionName);
                    p2FactionComboBox.setValue(clientFaction);
                    updateDeckStatusLabels();
                    checkIfBothFactionsSelected();
            } else if ("START_GAME".equals(message)) { // client received start signal => game start
                SoundService.getInstance().stopMenuMusic();
                navigationService.navigateTo(FXML_GAME_PATH, "Gwent", (GameController controller) -> {
                            GameService gameService = new GameService();
                            controller.setGameService(gameService);
                            controller.setNavigationService(navigationService);
                            controller.setLocalPlayerIdentity("Player 2");
                            // tell the game service to start as a client
                            gameService.startLanGameAsClient(client);
                        }
                );
            }
        });
    }

    // checks if both players have selected a faction => only the host can enable the start button
    private void checkIfBothFactionsSelected() {
        if (isHost) {
            boolean bothSelected = p1FactionComboBox.getValue() != null && p2FactionComboBox.getValue() != null;
            // enable start button only if both choices are known
            startGameButton.setDisable(!bothSelected);
            if (bothSelected) {
                p1DeckStatusLabel.setText("Beide Spieler bereit!");
                p2DeckStatusLabel.setText("Beide Spieler bereit!");
            }
        }
    }

    private void updateDeckStatusLabels() {
        updateStatusForPlayer(p1FactionComboBox.getValue(), p1DeckStatusLabel);
        if (gameMode != GameMode.PLAYER_VS_BOT) {
            updateStatusForPlayer(p2FactionComboBox.getValue(), p2DeckStatusLabel);
        }
    }

    private void updateStatusForPlayer(Faction faction, Label statusLabel) {
        if (faction == null) {
            // LAN Game
            if (isLanGame()) {
                if (statusLabel == p1DeckStatusLabel) {
                    statusLabel.setText(isHost ? "Wähle deine Fraktion" : "Warte auf Host...");
                } else {
                    statusLabel.setText(isHost ? "Warte auf Gegner..." : "Wähle deine Fraktion");
                }
            } else {
                statusLabel.setText("");
            }
            return;
        }
        DeckStatus status = getDeckStatus(faction);
        statusLabel.setText(status.message);
        statusLabel.getStyleClass().remove("deck-status-warning");
        statusLabel.getStyleClass().remove("deck-status-ok");
        statusLabel.getStyleClass().add(status.isValid ? "deck-status-ok" : "deck-status-warning"
        );
    }

    @FXML
    void handleStartGameAction(ActionEvent event) {
        Faction p1Faction = p1FactionComboBox.getValue();
        Faction p2Faction = p2FactionComboBox.getValue();

        if (p1Faction == null || p2Faction == null) {
            showAlert("Auswahl fehlt", "Bitte für beide Spieler eine Fraktion wählen.");
            return;
        }

        // starting a LAN game
        if (isLanGame()) {
            if (isHost) {
                // only the host can initiate the game start
                server.sendMessage("START_GAME");
                SoundService.getInstance().stopMenuMusic();
                navigationService.navigateTo(FXML_GAME_PATH, "Gwent", (GameController controller) -> {
                            GameService gameService = new GameService();
                            controller.setGameService(gameService);
                            controller.setNavigationService(navigationService);
                            controller.setLocalPlayerIdentity("Player 1");
                            // tell the game service to start as the host
                            gameService.startLanGameAsHost(server);
                            gameService.newGame(gameMode, BotDifficulty.NONE, p1Faction, p2Faction);
                        }
                );
            }
            return;
        }

        // local games (hotseat or PvB)
        SoundService.getInstance().stopMenuMusic();
        navigationService.navigateTo(
                FXML_GAME_PATH,
                "Gwent",
                (GameController controller) -> {
                    GameService gameService = new GameService();
                    controller.setGameService(gameService);
                    controller.setNavigationService(navigationService);
                    controller.setLocalPlayerIdentity("Player 1");
                    gameService.newGame(gameMode, botDifficulty, p1Faction, p2Faction);
                }
        );
    }

    // check if the current mode is lan
    private boolean isLanGame() {
        return server != null || client != null;
    }

    @FXML
    void handleBackAction(ActionEvent event) {
        // ensure network connections are closed before leaving
        if (server != null) {
            server.stop();
        }
        if (client != null) {
            client.stop();
        }

        if (navigationService != null) {
            navigationService.navigateTo(FXML_MAINMENU_PATH, "Main Menu", (MainMenuController controller) -> {controller.setNavigationService(navigationService);});
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private static class DeckStatus {
        final boolean isValid;
        final String message;

        DeckStatus(boolean isValid, String message) {
            this.isValid = isValid;
            this.message = message;
        }
    }

    private DeckStatus getDeckStatus(Faction faction) {
        if (faction == null) {
            return new DeckStatus(false, "Keine Fraktion ausgewählt");
        }
        List<Card> savedDeck = cardRepository.getSavedDeck(faction);
        if (!savedDeck.isEmpty()) {
            return new DeckStatus(true, String.format("Deck bereit (%d/%d)", savedDeck.size(), MIN_SELECTION));
        }
        long selectedCount = cardRepository.getAllCards().stream()
                .filter(c ->
                        c.getFaction() == faction &&
                                c.getSelectedAmount() > 0 &&
                                c.getAmount() > 0
                )
                .mapToInt(Card::getSelectedAmount)
                .sum();

        if (selectedCount > 0) {
            String msg = String.format("Deck unvollständig (%d/%d)", selectedCount, MIN_SELECTION);
            return new DeckStatus(false, msg);
        } else {
            return new DeckStatus(false, "Kein Deck ausgewählt");
        }
    }
}