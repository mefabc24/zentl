package org.example.demo3.view;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.example.demo3.event.*;
import org.example.demo3.model.board.Board;
import org.example.demo3.model.cards.Card;
import org.example.demo3.model.enums.*;
import org.example.demo3.model.player.Player;
import org.example.demo3.model.service.Cleanable;
import org.example.demo3.model.service.GameService;
import org.example.demo3.model.service.NavigationService;
import org.example.demo3.model.service.SoundService;

import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;

import static org.example.demo3.model.constants.Config.FXML_MAINMENU_PATH;

public class GameController implements Initializable, Cleanable {
    private final EventBus eventBus = EventBus.getInstanz();
    @FXML private VBox player1Side, player2Side, weatherArea, infoPanel;
    @FXML private HBox playerHand, Board;
    @FXML private Label roundLabel, currentPlayerLabel, p1Score, p2Score;
    @FXML private Button restartGameButton, backToMenuButton;

    private Player p1, p2, currentPlayer;
    private String localPlayerName; // needed to find the correct player object for first gamestate update (player2 doesnt exist before the first gamestate update comes from the server)
    private Player localPlayer; // gets updated with every gamestate update
    private int round;
    private Board gameBoard;
    private boolean gameEnded = false;
    private GameMode gameMode;
    private BotDifficulty botDifficulty;

    private NavigationService navigationService;
    private GameService gameService;

    private final EventHandler<GameStateUpdateEve> gameStateUpdateHandler = event -> Platform.runLater(() -> updateGameState(event));
    private final EventHandler<RoundEndedEve> roundEndedHandler = event -> Platform.runLater(() -> displayRoundResult(event));
    private final EventHandler<GameEndedEve> gameEndedHandler = event -> Platform.runLater(() -> displayGameOver(event));
    private final EventHandler<EffectLogEvent> effectLogHandler = event -> Platform.runLater(() -> showEffectLog(event.getMessage()));

    public void setNavigationService(NavigationService navigationService) {
        this.navigationService = navigationService;
    }

    public void setGameService(GameService gameService) {
        this.gameService = gameService;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        subscribeToEvents();
        restartGameButton.setVisible(false);
        backToMenuButton.setVisible(true);

        // Apply the class to the info panel labels to make their text black
        String blackTextClass = "game-board-label";
        roundLabel.getStyleClass().add(blackTextClass);
        currentPlayerLabel.getStyleClass().add(blackTextClass);
        p1Score.getStyleClass().add(blackTextClass);
        p2Score.getStyleClass().add(blackTextClass);
    }

    private void subscribeToEvents() {
        eventBus.subscribe(GameStateUpdateEve.class, gameStateUpdateHandler);
        eventBus.subscribe(RoundEndedEve.class, roundEndedHandler);
        eventBus.subscribe(GameEndedEve.class, gameEndedHandler);
        eventBus.subscribe(EffectLogEvent.class, effectLogHandler);
    }

    // sets the identity of the local player for lan games
    public void setLocalPlayerIdentity(String name) {
        this.localPlayerName = name;
    }

    @Override
    public void cleanup() {
        System.out.println("GameController cleaning up...");
        if (gameService != null) {
            // shut down server or client connections
            gameService.shutdownNetwork();
            gameService.shutdown();
        }
        eventBus.unsubscribe(GameStateUpdateEve.class, gameStateUpdateHandler);
        eventBus.unsubscribe(RoundEndedEve.class, roundEndedHandler);
        eventBus.unsubscribe(GameEndedEve.class, gameEndedHandler);
        eventBus.unsubscribe(EffectLogEvent.class, effectLogHandler);
    }

    @FXML
    void handleBackToMenu(ActionEvent event) {
        cleanup();
        SoundService.getInstance().startMenuMusic();
        if (navigationService != null) {
            navigationService.navigateTo(FXML_MAINMENU_PATH, "GWENT", controller -> {
                if (controller instanceof MainMenuController) {
                    ((MainMenuController) controller).setNavigationService(navigationService);
                }
            });
        }
    }

    @FXML
    void handleRestartGame(ActionEvent event) {
        cleanup();
        subscribeToEvents();
        gameEnded = false;
        this.gameService = new GameService();
        gameService.newGame(gameMode, botDifficulty, p1.getfraction(), p2.getfraction());
    }

    // updates the entire game state based on an event from the game service
    private void updateGameState(GameStateUpdateEve event) {
        if (this.gameMode == null) {
            this.gameMode = event.getGameMode();
            this.botDifficulty = event.getBotDifficulty();
        }

        this.p1 = event.getPlayer1();
        this.p2 = event.getPlayer2();
        this.currentPlayer = event.getCurrentPlayer();
        this.round = event.getRound();
        this.gameBoard = event.getGameBoard();

        // (LAN) determine which player object corresponds to the local user
        if (localPlayerName != null) {
            if (p1 != null && p1.getName().equals(localPlayerName)) {
                this.localPlayer = p1;
            } else if (p2 != null && p2.getName().equals(localPlayerName)) {
                this.localPlayer = p2;
            }
        }

        if (gameEnded) return;

        restartGameButton.setVisible(false);
        playerHand.setDisable(false);
        updateUI();
    }

    private void showEffectLog(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Card Effect");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.show();
    }

    private void updateUI() {
        if(p1 == null || p2 == null || currentPlayer == null) return;
        updatePlayerUI(player1Side, p1);
        updatePlayerUI(player2Side, p2);
        updateWeatherArea();
        updatePlayerHand();
        roundLabel.setText("Round: " + round);
        currentPlayerLabel.setText("Current: " + currentPlayer.getName() + (currentPlayer.hasPassed() ? " (Passed)" : ""));
        p1Score.setText(p1.getName() + " score: " + p1.getScore());
        p2Score.setText(p2.getName() + " score: " + p2.getScore());
    }

    private void updateWeatherArea() {
        weatherArea.getChildren().removeIf(node -> !(node instanceof Label));
        if (gameBoard != null) {
            for (Card card : gameBoard.getActiveWeatherCards()) {
                weatherArea.getChildren().add(createCardUI(card));
            }
        }
    }

    private void updatePlayerUI(VBox side, Player player) {
        side.getChildren().clear();
        Label name = new Label(player.getName() + " (Wins: " + player.getWins() + ")");
        name.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        // This label is on the dark background, so it should keep the default white text.
        // The incorrect line that added 'game-board-label' has been removed.
        side.getChildren().add(name);
        for (RowType row : List.of(RowType.MELEE, RowType.RANGED, RowType.SIEGE)) {
            side.getChildren().add(createRowUI(row, player));
        }
    }

    // updates the player's hand view => for lan to determine whose turn it is and who can play
    private void updatePlayerHand() {
        playerHand.getChildren().clear();

        if (currentPlayer == null) {
            playerHand.setDisable(true);
            return;
        }

        boolean canPlay;
        List<Card> handToShow;

        if (gameMode == GameMode.PLAYER_VS_PLAYER && (gameService != null && !gameService.isLanGame())) {
            // (hotseat)
            handToShow = currentPlayer.getHand();
            canPlay = !currentPlayer.hasPassed();

        } else if (gameMode == GameMode.PLAYER_VS_BOT) {
            // (PvB)
            handToShow = p1.getHand();
            canPlay = (currentPlayer == p1) && !p1.hasPassed();

        } else {
            // (LAN)
            if (localPlayer == null) {
                playerHand.setDisable(true);
                return;
            }
            handToShow = localPlayer.getHand();
            boolean isMyTurn = localPlayer.equals(currentPlayer);
            canPlay = isMyTurn && !localPlayer.hasPassed();
        }


        playerHand.setDisable(!canPlay);

        for (Card card : handToShow) {
            Node cardUI = createCardUI(card);
            if (canPlay) {
                cardUI.setOnMouseClicked(e -> {eventBus.post(new PlayCardRequest(card));});
            }
            playerHand.getChildren().add(cardUI);
        }

        if (canPlay) {
            Button passBtn = new Button("Pass");
            passBtn.setStyle("-fx-font-size: 14px; -fx-padding: 5px 15px;");
            passBtn.setOnAction(e -> eventBus.post(new PlayerPassed()));
            playerHand.getChildren().add(passBtn);
        }
    }

    private VBox createRowUI(RowType rowType, Player player) {
        VBox rowBox = new VBox(5);
        rowBox.setAlignment(Pos.CENTER);
        rowBox.setStyle("-fx-background-color: " + getRowColor(rowType) + "; -fx-padding: 5px; -fx-min-width: 300px;");

        Label rowLabel = new Label(rowType.name());
        rowLabel.setStyle("-fx-font-weight: bold;");
        // Add the new style class to this label to make the text black
        rowLabel.getStyleClass().add("game-board-label");

        HBox cardsBox = new HBox(5);
        cardsBox.setAlignment(Pos.CENTER);
        List<Card> cards = gameBoard.getPlayerRows(player).getOrDefault(rowType, Collections.emptyList());
        for (Card card : cards) {
            cardsBox.getChildren().add(createCardUI(card));
        }

        int power = gameBoard.calculateRowPower(rowType, player);
        Label powerLabel = new Label("Power: " + power);
        powerLabel.setStyle("-fx-font-weight: bold;");
        powerLabel.getStyleClass().add("game-board-label");

        rowBox.getChildren().addAll(rowLabel, cardsBox, powerLabel);
        return rowBox;
    }

    private Node createCardUI(Card card) {
        VBox box = new VBox(3);
        box.setAlignment(Pos.CENTER);
        box.getStyleClass().add("game-card");

        Label name = new Label(card.getName());
        name.setStyle("-fx-font-weight: bold;");
        name.getStyleClass().add("game-card-label");

        String powerText = (card.getCardType() == CardType.UNIT) ? "Power: " + card.getPower() : "Effect";
        Label power = new Label(powerText);
        power.getStyleClass().add("game-card-label");

        box.getChildren().addAll(name, power);
        return box;
    }

    private void displayRoundResult(RoundEndedEve event) {
        playerHand.setDisable(true);
        playerHand.getChildren().clear();
        p1Score.setText(event.getP1().getName() + " Score: " + event.getP1().getScore());
        p2Score.setText(event.getP2().getName() + " Score: " + event.getP2().getScore());
        String winner = (event.getWinner() != null) ? event.getWinner().getName() : "Draw: Counts as win for both";
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Round over");
        alert.setHeaderText("Round " + event.getRound() + " over");
        alert.setContentText("Round Winner: " + winner + "\n\n" + event.getP1().getName() + ": " + event.getP1().getScore() + " (Wins: " + event.getP1().getWins() + ")\n" + event.getP2().getName() + ": " + event.getP2().getScore() + " (Wins: " + event.getP2().getWins() + ")");
        alert.showAndWait();
    }

    private void displayGameOver(GameEndedEve event) {
        gameEnded = true;

        if (gameService != null) gameService.shutdown();

        p1Score.setText(event.getP1Name() + " Score: " + event.getP1().getScore() + " (Wins: " + event.getPl1Wins() + ")");
        p2Score.setText(event.getP2Name() + " score: " + event.getP2().getScore() + " (Wins: " + event.getP2Wins() + ")");
        String winner = (event.getWinner() != null) ? event.getWinner().getName() : "Draw";
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Game Over");
        alert.setHeaderText("Game Over");
        alert.setContentText("Winner: " + winner + "\nFinal Score:\n" + event.getP1Name() + ": " + event.getPl1Wins() + " Wins\n" + event.getP2Name() + ": " + event.getP2Wins() + " Wins\n");
        alert.showAndWait();
        currentPlayerLabel.setText("Game Over");
        playerHand.setDisable(true);
        playerHand.getChildren().clear();
        restartGameButton.setVisible(true);
    }

    private String getRowColor(RowType rowType) {
        return switch (rowType) {
            case MELEE -> "#ffdddd";
            case RANGED -> "#ddffdd";
            case SIEGE -> "#ddddff";
            default -> "#ffffff";
        };
    }
}