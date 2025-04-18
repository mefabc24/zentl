package org.example.demo3.ui;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.example.demo3.event.*;
import org.example.demo3.model.board.GameBoard;
import org.example.demo3.model.cards.Card;
import org.example.demo3.model.enums.RowType;
import org.example.demo3.model.player.Player;

import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;

public class GameController implements Initializable {
    private final EventBus eventBus = EventBus.getInstance();
    @FXML
    private VBox player1Side;
    @FXML
    private VBox player2Side;
    @FXML
    private HBox playerHand;
    @FXML
    private Label roundLabel;
    @FXML
    private Label currentPlayerLabel;
    @FXML
    private Label p1Score;
    @FXML
    private Label p2Score;
    @FXML
    private Button restartGameButton;
    private Player player1;
    private Player player2;
    private Player currentPlayer;
    private int round;
    private GameBoard gameBoard;
    private boolean gameEnded = false;

    public void initialize(URL location, ResourceBundle resources) {
        registerEventHandlers();
        restartGameButton.setOnAction(e -> {
            gameEnded = false;
            eventBus.post(new RestartEve());
        });
        restartGameButton.setVisible(false);
    }

    private void registerEventHandlers() {
        eventBus.subscribe(GameStateUpdateEve.class, this::handleGameStateUpdate);
        eventBus.subscribe(RoundEndedEve.class, this::handleRoundEnded);
        eventBus.subscribe(GameEndedEve.class, this::handleGameEnded);
    }

    private void handleGameStateUpdate(GameStateUpdateEve event) {
        this.player1 = event.getPlayer1();
        this.player2 = event.getPlayer2();
        this.currentPlayer = event.getCurrentPlayer();
        this.round = event.getRound();
        this.gameBoard = event.getGameBoard();
        if (!gameEnded) {
            playerHand.setDisable(false);
            restartGameButton.setVisible(false);
            updateUI();
        }
    }

    private void handleRoundEnded(RoundEndedEve event) {
        p1Score.setText(event.getP1().getName() + " score: " + event.getP1().getScore());
        p2Score.setText(event.getP2().getName() + " score: " + event.getP2().getScore());
        playerHand.setDisable(true);
        playerHand.getChildren().clear();
        String winnerName = (event.getWinner() != null) ? event.getWinner().getName() : "Draw";
        Alert alert = getAlert(event, winnerName);
        alert.showAndWait();
    }

    private void handleGameEnded(GameEndedEve event) {
        gameEnded = true;
        p1Score.setText(event.getPlayer1Name() + " score: " + player1.getScore() + " (Wins: " + event.getPlayer1Wins() + ")");
        p2Score.setText(event.getPlayer2Name() + " score: " + player2.getScore() + " (Wins: " + event.getPlayer2Wins() + ")");
        String winnerName = (event.getWinner() != null) ? event.getWinner().getName() : "Draw - no clear winner";
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Game Over");
        alert.setHeaderText("Match Finished");
        alert.setContentText("Overall Winner: " + winnerName + "\n\nFinal Score:\n" + event.getPlayer1Name() + ": " + event.getPlayer1Wins() + " wins\n" + event.getPlayer2Name() + ": " + event.getPlayer2Wins() + " wins");
        alert.showAndWait();
        playerHand.setDisable(true);
        playerHand.getChildren().clear();
        restartGameButton.setVisible(true);
        currentPlayerLabel.setText("Game Over");
    }

    private Alert getAlert(RoundEndedEve roundEndedEve, String winnerName) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Round Over");
        alert.setHeaderText("Round " + roundEndedEve.getRound() + " finished");
        alert.setContentText("Round Winner: " + winnerName + "\n" + roundEndedEve.getP1().getName() + ": " + roundEndedEve.getP1().getScore() + " (Wins: " + roundEndedEve.getP1().getWins() + ")\n" + roundEndedEve.getP2().getName() + ": " + roundEndedEve.getP2().getScore() + " (Wins: " + roundEndedEve.getP2().getWins() + ")");
        return alert;
    }

    private void updateUI() {
        if (player1 == null || player2 == null || currentPlayer == null || gameBoard == null) {
            return;
        }
        updatePlayerSide(player1Side, player1);
        updatePlayerSide(player2Side, player2);
        updateHandUI();
        roundLabel.setText("Round: " + this.round);
        currentPlayerLabel.setText("Current: " + this.currentPlayer.getName() + (this.currentPlayer.hasPassed() ? " (Passed)" : ""));
        p1Score.setText(this.player1.getName() + " score: " + this.player1.getScore());
        p2Score.setText(this.player2.getName() + " score: " + this.player2.getScore());
    }

    private void updatePlayerSide(VBox playerSide, Player player) {
        playerSide.getChildren().clear();
        Label playerName = new Label(player.getName() + " (Wins: " + player.getWins() + ")");
        playerName.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        playerSide.getChildren().add(playerName);
        VBox meleeRow = createRowUI(RowType.MELEE, player);
        VBox rangedRow = createRowUI(RowType.RANGED, player);
        VBox siegeRow = createRowUI(RowType.SIEGE, player);
        playerSide.getChildren().addAll(meleeRow, rangedRow, siegeRow);
    }

    private VBox createRowUI(RowType rowType, Player player) {
        VBox rowBox = new VBox(5);
        rowBox.setAlignment(Pos.CENTER);
        rowBox.setStyle("-fx-background-color: " + getRowColor(rowType) + "; -fx-padding: 5px;");
        Label rowLabel = new Label(rowType.toString());
        rowLabel.setStyle("-fx-font-weight: bold;");
        HBox cardsBox = new HBox(5);
        cardsBox.setAlignment(Pos.CENTER);
        if (this.gameBoard != null) {
            List<Card> cards = this.gameBoard.getPlayerRows(player).getOrDefault(rowType, Collections.emptyList());
            for (Card card : cards) {
                VBox cardUI = createCardUI(card);
                cardsBox.getChildren().add(cardUI);
            }
            int rowPower = this.gameBoard.calculateRowPower(rowType, player);
            Label powerLabel = new Label("Power: " + rowPower);
            powerLabel.setStyle("-fx-font-weight: bold;");
            cardsBox.getChildren().add(powerLabel);
        } else {
            cardsBox.getChildren().add(new Label("Board state not available"));
        }
        rowBox.getChildren().addAll(rowLabel, cardsBox);
        return rowBox;
    }

    private VBox createCardUI(Card card) {
        VBox cardBox = new VBox(3);
        cardBox.setStyle("-fx-background-color: #f0f0f0; -fx-border-color: #333; -fx-border-width: 1px; -fx-padding: 5px;");
        cardBox.setAlignment(Pos.CENTER);
        Label nameLabel = new Label(card.getName());
        nameLabel.setStyle("-fx-font-weight: bold;");
        String powerText = (card instanceof org.example.demo3.model.cards.UnitCard) ? "Power: " + card.getPower() : "Special";
        Label powerLabel = new Label(powerText);
        cardBox.getChildren().addAll(nameLabel, powerLabel);
        cardBox.setOnMouseEntered(e -> cardBox.setStyle("-fx-background-color: #e0e0e0; -fx-border-color: #000; -fx-border-width: 2px; -fx-padding: 5px;"));
        cardBox.setOnMouseExited(e -> cardBox.setStyle("-fx-background-color: #f0f0f0; -fx-border-color: #333; -fx-border-width: 1px; -fx-padding: 5px;"));
        return cardBox;
    }

    private void updateHandUI() {
        playerHand.getChildren().clear();
        if (currentPlayer == null || player1 == null || player2 == null) {
            return;
        }
        boolean roundIsOver = player1.hasPassed() && player2.hasPassed();
        if (roundIsOver || gameEnded) {
            playerHand.setDisable(true);
            return;
        }
        playerHand.setDisable(currentPlayer.hasPassed());
        for (Card card : currentPlayer.getHand()) {
            VBox cardUI = createCardUI(card);
            if (!currentPlayer.hasPassed()) {
                cardUI.setOnMouseClicked(e -> {
                    System.out.println("Attempting to play card: " + card.getName() + " by " + currentPlayer.getName());
                    eventBus.post(new PlayCardCommand(currentPlayer, card));
                });
            }
            playerHand.getChildren().add(cardUI);
        }
        if (!currentPlayer.hasPassed()) {
            Button passButton = new Button("Pass");
            passButton.setStyle("-fx-font-size: 14px; -fx-padding: 5px 15px;");
            passButton.setOnAction(e -> eventBus.post(new PassCommand(currentPlayer)));
            playerHand.getChildren().add(passButton);
        }
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