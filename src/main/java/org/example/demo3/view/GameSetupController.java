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

public class GameSetupController {

    @FXML private ComboBox<Faction> p1FactionComboBox;
    @FXML private Label p1DeckStatusLabel;
    @FXML private ComboBox<Faction> p2FactionComboBox;
    @FXML private Label p2DeckStatusLabel;
    @FXML private Button startGameButton;
    @FXML private Button backButton;
    // Player Labels
    @FXML private Label player1Label;
    @FXML private Label player2Label;

    private NavigationService navigationService;
    private CardRepository cardRepository;
    private GameMode gameMode;
    private BotDifficulty bot1Difficulty;
    private BotDifficulty bot2Difficulty;

    private boolean isHost; // flag to determine if this player is the host or client
    private Server server;
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

        p1FactionComboBox.setItems(
                FXCollections.observableArrayList(playableFactions)
        );
        p2FactionComboBox.setItems(
                FXCollections.observableArrayList(playableFactions)
        );

        p1FactionComboBox
                .valueProperty()
                .addListener((obs, oldVal, newVal) -> {
                    if (isLanGame()) {
                        if (newVal != null) {
                            sendFactionChoice(newVal);
                            updateDeckStatusLabels();
                            checkIfBothFactionsSelected();
                        }
                    } else {
                        updateDeckStatusLabels();
                    }
                });
        p2FactionComboBox
                .valueProperty()
                .addListener((obs, oldVal, newVal) -> {
                    if (isLanGame()) {
                        if (newVal != null) {
                            sendFactionChoice(newVal);
                            updateDeckStatusLabels();
                        }
                    } else {
                        updateDeckStatusLabels();
                    }
                });
    }

    // Main init method to handle all local modes
    public void initData(
            GameMode mode,
            BotDifficulty b1Diff,
            BotDifficulty b2Diff
    ) {
        this.gameMode = mode;
        this.bot1Difficulty = b1Diff;
        this.bot2Difficulty = b2Diff;

        p1FactionComboBox.setValue(Faction.KNIGHTS);
        p2FactionComboBox.setValue(Faction.MONSTERS);

        // Configure UI based on the game mode
        switch (mode) {
            case PLAYER_VS_PLAYER:
                player1Label.setText("Player 1");
                player2Label.setText("Player 2");
                p1FactionComboBox.setDisable(false);
                p2FactionComboBox.setDisable(false);
                break;
            case PLAYER_VS_BOT:
                player1Label.setText("Player");
                player2Label.setText("Bot (" + bot1Difficulty.name() + ")");
                p1FactionComboBox.setDisable(false);
                p2FactionComboBox.setDisable(true);
                p2DeckStatusLabel.setText("Bot will use a random deck");
                break;
            case BOT_VS_BOT:
                player1Label.setText("Bot 1 (" + bot1Difficulty.name() + ")");
                player2Label.setText("Bot 2 (" + bot2Difficulty.name() + ")");
                p1FactionComboBox.setDisable(true);
                p2FactionComboBox.setDisable(true);
                p1DeckStatusLabel.setText("Bot will use a random deck");
                p2DeckStatusLabel.setText("Bot will use a random deck");
                break;
        }
        updateDeckStatusLabels();
    }

    // initializes the view for a lan game, configures UI based on if the player is host or client
    public void initLanData(
            NavigationService navigationService,
            boolean isHost,
            Server server,
            Client client
    ) {
        setNavigationService(navigationService);
        this.isHost = isHost;
        this.server = server;
        this.client = client;
        this.gameMode = GameMode.PLAYER_VS_PLAYER;

        if (isHost) {
            // setup host
            player1Label.setText("Player 1 (You)");
            player2Label.setText("Player 2 (Opponent)");
            p1FactionComboBox.setDisable(false);
            p2FactionComboBox.setDisable(true);
            startGameButton.setDisable(true);
            p1DeckStatusLabel.setText("Choose your faction");
            p2DeckStatusLabel.setText("Waiting for opponent...");
            this.server.setOnMessageReceived(this::handleNetworkMessage);
        } else {
            // setup client
            player1Label.setText("Player 1 (Opponent)");
            player2Label.setText("Player 2 (You)");
            p1FactionComboBox.setDisable(true);
            p2FactionComboBox.setDisable(false);
            startGameButton.setDisable(true);
            p1DeckStatusLabel.setText("Waiting for host...");
            p2DeckStatusLabel.setText("Choose your faction");
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
            if (message.startsWith("HOST_FACTION:")) {
                String factionName = message.substring("HOST_FACTION:".length());
                Faction hostFaction = Faction.valueOf(factionName);
                p1FactionComboBox.setValue(hostFaction);
                updateDeckStatusLabels();
                checkIfBothFactionsSelected();
            } else if (message.startsWith("CLIENT_FACTION:")) {
                String factionName = message.substring(
                        "CLIENT_FACTION:".length()
                );
                Faction clientFaction = Faction.valueOf(factionName);
                p2FactionComboBox.setValue(clientFaction);
                updateDeckStatusLabels();
                checkIfBothFactionsSelected();
            } else if ("START_GAME".equals(message)) {
                // client received start signal => game start
                SoundService.getInstance().stopMenuMusic();
                navigationService.navigateTo(
                        FXML_GAME_PATH,
                        "Gwent",
                        (GameController controller) -> {
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
            boolean bothSelected =
                    p1FactionComboBox.getValue() != null &&
                            p2FactionComboBox.getValue() != null;
            // enable start button only if both choices are known
            startGameButton.setDisable(!bothSelected);
            if (bothSelected) {
                p1DeckStatusLabel.setText("Both players ready!");
                p2DeckStatusLabel.setText("Both players ready!");
            }
        }
    }

    private void updateDeckStatusLabels() {
        if (gameMode != GameMode.BOT_VS_BOT) {
            updateStatusForPlayer(
                    p1FactionComboBox.getValue(),
                    p1DeckStatusLabel
            );
        }
        if (gameMode == GameMode.PLAYER_VS_PLAYER) {
            updateStatusForPlayer(
                    p2FactionComboBox.getValue(),
                    p2DeckStatusLabel
            );
        }
    }

    private void updateStatusForPlayer(Faction faction, Label statusLabel) {
        if (isLanGame() && faction == null) {
            return;
        }

        DeckStatus status = getDeckStatus(faction);
        statusLabel.setText(status.message);
        statusLabel.getStyleClass().remove("deck-status-warning");
        statusLabel.getStyleClass().remove("deck-status-ok");
        statusLabel
                .getStyleClass()
                .add(status.isValid ? "deck-status-ok" : "deck-status-warning");
    }

    @FXML
    void handleStartGameAction(ActionEvent event) {
        Faction p1Faction = p1FactionComboBox.getValue();
        Faction p2Faction = p2FactionComboBox.getValue();

        if (p1Faction == null || p2Faction == null) {
            showAlert(
                    "Selection Missing",
                    "Please select a faction for both players."
            );
            return;
        }

        // starting a LAN game
        if (isLanGame()) {
            if (isHost) {
                // only the host can initiate the game start
                server.sendMessage("START_GAME");
                SoundService.getInstance().stopMenuMusic();
                navigationService.navigateTo(
                        FXML_GAME_PATH,
                        "Gwent",
                        (GameController controller) -> {
                            GameService gameService = new GameService();
                            controller.setGameService(gameService);
                            controller.setNavigationService(navigationService);
                            controller.setLocalPlayerIdentity("Player 1");
                            // tell the game service to start as the host
                            gameService.startLanGameAsHost(server);
                            gameService.newGame(
                                    gameMode,
                                    BotDifficulty.NONE,
                                    BotDifficulty.NONE,
                                    p1Faction,
                                    p2Faction
                            );
                        }
                );
            }
            return;
        }

        // local games (Hotseat, PvB, BvB)
        if (gameMode == GameMode.PLAYER_VS_PLAYER && p1Faction == p2Faction) {
            showAlert(
                    "Invalid Selection",
                    "Players cannot choose the same faction in a local match."
            );
            return;
        }

        SoundService.getInstance().stopMenuMusic();
        navigationService.navigateTo(
                FXML_GAME_PATH,
                "Gwent",
                (GameController controller) -> {
                    GameService gameService = new GameService();
                    controller.setGameService(gameService);
                    controller.setNavigationService(navigationService);
                    gameService.newGame(
                            gameMode,
                            this.bot1Difficulty,
                            this.bot2Difficulty,
                            p1Faction,
                            p2Faction
                    );
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
            navigationService.navigateTo(
                    FXML_MAINMENU_PATH,
                    "Main Menu",
                    (MainMenuController controller) -> {
                        controller.setNavigationService(navigationService);
                    }
            );
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
            return new DeckStatus(false, "⚠️ No faction selected");
        }
        List<Card> savedDeck = cardRepository.getSavedDeck(faction);
        if (!savedDeck.isEmpty()) {
            return new DeckStatus(
                    true,
                    String.format(
                            "✓ Deck ready (%d/%d)",
                            savedDeck.size(),
                            MIN_SELECTION
                    )
            );
        }
        long selectedCount = cardRepository
                .getAllCards()
                .stream()
                .filter(c ->
                        c.getFaction() == faction &&
                                c.getSelectedAmount() > 0 &&
                                c.getAmount() > 0
                )
                .mapToInt(Card::getSelectedAmount)
                .sum();

        if (selectedCount > 0) {
            String msg = String.format(
                    "⚠️ Deck incomplete (%d/%d)",
                    selectedCount,
                    MIN_SELECTION
            );
            return new DeckStatus(false, msg);
        } else {
            return new DeckStatus(false, "⚠️ No deck selected");
        }
    }
}