package org.example.demo3.view;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
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
import org.example.demo3.model.service.GameService;
import org.example.demo3.model.service.NavigationService;
import org.example.demo3.model.service.SoundService;

import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;

import static org.example.demo3.model.constants.Config.FXML_MAINMENU_PATH;

public class GameController implements Initializable {
    private final EventBus eventBus = EventBus.getInstanz();
    @FXML private VBox player1Side, player2Side, weatherArea, infoPanel;
    @FXML private HBox playerHand, Board;
    @FXML private Label roundLabel, currentPlayerLabel, p1Score, p2Score;
    @FXML private Button restartGameButton, backToMenuButton;

    private Player p1, p2, currentPlayer;
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
    }

    private void subscribeToEvents() {
        eventBus.subscribe(GameStateUpdateEve.class, gameStateUpdateHandler);
        eventBus.subscribe(RoundEndedEve.class, roundEndedHandler);
        eventBus.subscribe(GameEndedEve.class, gameEndedHandler);
        eventBus.subscribe(EffectLogEvent.class, effectLogHandler);
    }

    private void cleanup() {
        System.out.println("GameController cleaning up...");
        if (gameService != null) {
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
        // 1. Clean up the old game and its listeners.
        // This shuts down the old gameService and unsubscribes its event handlers.
        cleanup();

        // 2. Re-subscribe the UI listeners of this controller for the new game.
        subscribeToEvents();

        // 3. Reset the game-over flag.
        gameEnded = false;
        
        // This ensures a clean state, and its constructor will subscribe its own event handlers once.
        this.gameService = new GameService();

        // 5. Tell the new service to start a new game with the previous settings.
        // We use the p1 and p2 objects from the last game state just to get their faction info.
        gameService.newGame(gameMode, botDifficulty, p1.getfraction(), p2.getfraction());
    }

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
        side.getChildren().add(name);
        for (RowType row : List.of(RowType.MELEE, RowType.RANGED, RowType.SIEGE)) {
            side.getChildren().add(createRowUI(row, player));
        }
    }

    private void updatePlayerHand() {
        playerHand.getChildren().clear();
        boolean roundOver = p1 != null && p2 != null && p1.hasPassed() && p2.hasPassed();
        if (roundOver || gameEnded) {
            playerHand.setDisable(true);
            return;
        }
        if (currentPlayer == null) return;

        playerHand.setDisable(currentPlayer.hasPassed());

        for (Card card : currentPlayer.getHand()) {
            VBox cardUI = createCardUI(card);
            if (!currentPlayer.hasPassed()) {
                cardUI.setOnMouseClicked(e -> eventBus.post(new PlayCardRequest(currentPlayer, card)));
            }
            playerHand.getChildren().add(cardUI);
        }

        if (!currentPlayer.hasPassed()) {
            Button passBtn = new Button("Pass");
            passBtn.setStyle("-fx-font-size: 14px; -fx-padding: 5px 15px;");
            passBtn.setOnAction(e -> eventBus.post(new PlayerPassed(currentPlayer)));
            playerHand.getChildren().add(passBtn);
        }
    }

    private VBox createRowUI(RowType rowType, Player player) {
        VBox rowBox = new VBox(5);
        rowBox.setAlignment(Pos.CENTER);
        rowBox.setStyle("-fx-background-color: " + getRowColor(rowType) + "; -fx-padding: 5px; -fx-min-width: 300px;");
        Label rowLabel = new Label(rowType.name());
        rowLabel.setStyle("-fx-font-weight: bold;");
        HBox cardsBox = new HBox(5);
        cardsBox.setAlignment(Pos.CENTER);
        List<Card> cards = gameBoard.getPlayerRows(player).getOrDefault(rowType, Collections.emptyList());
        for (Card card : cards) {
            cardsBox.getChildren().add(createCardUI(card));
        }
        int power = gameBoard.calculateRowPower(rowType, player);
        Label powerLabel = new Label("Power: " + power);
        powerLabel.setStyle("-fx-font-weight: bold;");
        rowBox.getChildren().addAll(rowLabel, cardsBox, powerLabel);
        return rowBox;
    }

    private VBox createCardUI(Card card) {
        VBox box = new VBox(3);
        box.setAlignment(Pos.CENTER);
        box.setStyle("-fx-background-color: #f0f0f0; -fx-border-color: #333; -fx-border-width: 1px; -fx-padding: 5px; -fx-cursor: hand;");
        Label name = new Label(card.getName());
        name.setStyle("-fx-font-weight: bold;");
        String powerText = (card.getCardType() == CardType.UNIT) ? "Power: " + card.getPower() : "Effect";
        Label power = new Label(powerText);
        box.getChildren().addAll(name, power);
        box.setOnMouseEntered(e -> box.setStyle("-fx-background-color: #e0e0e0; -fx-border-color: #000; -fx-border-width: 2px; -fx-padding: 5px; -fx-cursor: hand;"));
        box.setOnMouseExited(e -> box.setStyle("-fx-background-color: #f0f0f0; -fx-border-color: #333; -fx-border-width: 1px; -fx-padding: 5px; -fx-cursor: hand;"));
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