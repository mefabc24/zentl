package org.example.demo3.view;

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
import org.example.demo3.model.enums.RowType;
import org.example.demo3.model.player.Player;

import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;

public class GameController implements Initializable {
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

    private Player p1;
    private Player p2;
    private int round;
    private Player currentPlayer;
    private Board gameBoard;
    private boolean gameEnded = false;
    private final EventBus eventBus = EventBus.getInstance();

    public void initialize(URL location, ResourceBundle resources) {
        subscribeToEvents();
        restartGameButton.setOnAction(e -> {
            gameEnded = false;
            eventBus.post(new RestartEve());
        });
        restartGameButton.setVisible(false);
    }

    private void subscribeToEvents() {
        eventBus.subscribe(GameStateUpdateEve.class, this::updateGameState);
        eventBus.subscribe(RoundEndedEve.class, this::showRoundResult);
        eventBus.subscribe(GameEndedEve.class, this::showGameOver);
    }

    private void updateGameState(GameStateUpdateEve event) {
        this.p1 = event.getPlayer1();
        this.p2 = event.getPlayer2();
        this.currentPlayer = event.getCurrentPlayer();
        this.round = event.getRound();
        this.gameBoard = event.getGameBoard();
        if (gameEnded) return;
        playerHand.setDisable(false);
        restartGameButton.setVisible(false);
        updateUI();
    }

    private void updateUI() {
        updatePlayerUI(player1Side, p1);
        updatePlayerUI(player2Side, p2);
        updatePlayerHand();
        roundLabel.setText("Round: " + round);
        currentPlayerLabel.setText("Current: " + currentPlayer.getName() + (currentPlayer.hasPassed() ? " (Passed)" : ""));
        p1Score.setText(p1.getName() + " score: " + p1.getScore());
        p2Score.setText(p2.getName() + " score: " + p2.getScore());
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

    private VBox createRowUI(RowType rowType, Player player) {
        VBox rowBox = new VBox(5);
        rowBox.setAlignment(Pos.CENTER);
        rowBox.setStyle("-fx-background-color: " + getRowColor(rowType) + "; -fx-padding: 5px;");
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
        cardsBox.getChildren().add(powerLabel);
        rowBox.getChildren().addAll(rowLabel, cardsBox);
        return rowBox;
    }

    private void updatePlayerHand() {
        playerHand.getChildren().clear();
        boolean roundOver = p1.hasPassed() && p2.hasPassed();
        if (roundOver || gameEnded) {
            playerHand.setDisable(true);
            return;
        }
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

    private VBox createCardUI(Card card) {
        VBox box = new VBox(3);
        box.setAlignment(Pos.CENTER);
        box.setStyle("-fx-background-color: #f0f0f0; -fx-border-color: #333; -fx-border-width: 1px; -fx-padding: 5px;");
        Label name = new Label(card.getName());
        name.setStyle("-fx-font-weight: bold;");
        String powerText = (card instanceof org.example.demo3.model.cards.UnitCard) ? "Power: " + card.getPower() : "Special";
        Label power = new Label(powerText);
        box.getChildren().addAll(name, power);
        box.setOnMouseEntered(e -> box.setStyle("-fx-background-color: #e0e0e0; -fx-border-color: #000; -fx-border-width: 2px; -fx-padding: 5px;"));
        box.setOnMouseExited(e -> box.setStyle("-fx-background-color: #f0f0f0; -fx-border-color: #333; -fx-border-width: 1px; -fx-padding: 5px;"));
        return box;
    }

    private void showRoundResult(RoundEndedEve event) {
        playerHand.setDisable(true);
        playerHand.getChildren().clear();
        p1Score.setText(event.getP1().getName() + " score: " + event.getP1().getScore());
        p2Score.setText(event.getP2().getName() + " score: " + event.getP2().getScore());
        String winnerName = (event.getWinner() != null) ? event.getWinner().getName() : "Draw";
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Round Over");
        alert.setHeaderText("Round " + event.getRound() + " finished");
        alert.setContentText("Round Winner: " + winnerName + "\n\n" + event.getP1().getName() + ": " + event.getP1().getScore() + " (Wins: " + event.getP1().getWins() + ")\n" + event.getP2().getName() + ": " + event.getP2().getScore() + " (Wins: " + event.getP2().getWins() + ")");
        alert.showAndWait();
    }

    private void showGameOver(GameEndedEve event) {
        gameEnded = true;
        p1Score.setText(event.getPlayer1Name() + " score: " + p1.getScore() + " (Wins: " + event.getPlayer1Wins() + ")");
        p2Score.setText(event.getPlayer2Name() + " score: " + p2.getScore() + " (Wins: " + event.getPlayer2Wins() + ")");
        String winner = (event.getWinner() != null) ? event.getWinner().getName() : "Draw - no clear winner";
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Game Over");
        alert.setHeaderText("Match Finished");
        alert.setContentText("Overall Winner: " + winner + "\n\nFinal Score:\n" + event.getPlayer1Name() + ": " + event.getPlayer1Wins() + " wins\n" + event.getPlayer2Name() + ": " + event.getPlayer2Wins() + " wins");
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