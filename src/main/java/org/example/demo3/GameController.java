package org.example.demo3;

import org.example.demo3.model.*;
import org.example.demo3.model.enums.*;
import org.example.demo3.service.*;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.geometry.*;
import org.example.demo3.service.EventListener;

import java.net.URL;
import java.util.*;

public class GameController implements Initializable, EventListener {
    @FXML private VBox player1Side;
    @FXML private VBox player2Side;
    @FXML private HBox playerHand;
    @FXML private Label roundLabel;
    @FXML private Label currentPlayerLabel;
    @FXML private Label p1Score;
    @FXML private Label p2Score;
    @FXML private Button newRoundButton;
    @FXML private Button restartGameButton;

    private GameService gameService;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        gameService = new GameServiceImpl();
        gameService.registerListener(this); // Registrieren des Frontends als Listener

        newRoundButton.setOnAction(e -> gameService.startNewRound());
        restartGameButton.setOnAction(e -> gameService.restartGame());
        restartGameButton.setVisible(false);
    }

    public void initializeGame() {
        gameService.initializeGame();
    }

    private void updateUI() {
        updatePlayerSide(player1Side, gameService.getPlayer1());
        updatePlayerSide(player2Side, gameService.getPlayer2());
        updateHandUI();

        roundLabel.setText("Round: " + gameService.getRound());
        currentPlayerLabel.setText("Current: " + gameService.getCurrentPlayer().getName());
        p1Score.setText(gameService.getPlayer1().getName() + " score: " +
                gameService.getPlayerScore(gameService.getPlayer1()));
        p2Score.setText(gameService.getPlayer2().getName() + " score: " +
                gameService.getPlayerScore(gameService.getPlayer2()));
    }

    private void updatePlayerSide(VBox playerSide, Player player) {
        playerSide.getChildren().clear();

        Label playerName = new Label(player.getName());
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

        // Hier GameBoard über GameService abrufen
        GameBoard gameBoard = gameService.getGameBoard();
        List<Card> cards = gameBoard.getPlayerRows(player).getOrDefault(rowType, Collections.emptyList());
        for (Card card : cards) {
            VBox cardUI = createCardUI(card);
            cardsBox.getChildren().add(cardUI);
        }

        int rowPower = gameBoard.calculateRowPower(rowType, player);
        Label powerLabel = new Label("Power: " + rowPower);
        powerLabel.setStyle("-fx-font-weight: bold;");
        cardsBox.getChildren().add(powerLabel);

        rowBox.getChildren().addAll(rowLabel, cardsBox);
        return rowBox;
    }

    private VBox createCardUI(Card card) {
        VBox cardBox = new VBox(3);
        cardBox.setStyle("-fx-background-color: #f0f0f0; -fx-border-color: #333; -fx-border-width: 1px; -fx-padding: 5px;");
        cardBox.setAlignment(Pos.CENTER);

        Label nameLabel = new Label(card.getName());
        nameLabel.setStyle("-fx-font-weight: bold;");

        Label powerLabel = new Label("Power: " + card.getPower());

        Label typeLabel = new Label(card.getType().toString());
        typeLabel.setStyle("-fx-font-size: 10px;");

        cardBox.getChildren().addAll(nameLabel, powerLabel, typeLabel);

        cardBox.setOnMouseEntered(e -> cardBox.setStyle("-fx-background-color: #e0e0e0; -fx-border-color: #000; -fx-border-width: 2px; -fx-padding: 5px;"));
        cardBox.setOnMouseExited(e -> cardBox.setStyle("-fx-background-color: #f0f0f0; -fx-border-color: #333; -fx-border-width: 1px; -fx-padding: 5px;"));

        return cardBox;
    }

    private void updateHandUI() {
        playerHand.getChildren().clear();

        if (gameService.isRoundOver()) {
            playerHand.setDisable(true);
            return;
        }

        Player currentPlayer = gameService.getCurrentPlayer();
        if (currentPlayer.hasPassed()) return;

        for (Card card : currentPlayer.getHand()) {
            VBox cardUI = createCardUI(card);
            cardUI.setOnMouseClicked(e -> gameService.playCard(currentPlayer, card));
            playerHand.getChildren().add(cardUI);
        }
        Button passButton = new Button("Pass");
        passButton.setStyle("-fx-font-size: 14px; -fx-padding: 5px 15px;");
        passButton.setOnAction(e -> gameService.playerPass(currentPlayer));
        playerHand.getChildren().add(passButton);
    }

    private String getRowColor(RowType rowType) {
        return switch (rowType) {
            case MELEE -> "#ffdddd";
            case RANGED -> "#ddffdd";
            case SIEGE -> "#ddddff";
            default -> "#ffffff";
        };
    }

    // Methoden für die verschiedenen Events, werden von Gameservice aufgerufen

    @Override
    public void onGameInitialized() {
        newRoundButton.setDisable(false);
        playerHand.setDisable(false);
        restartGameButton.setVisible(false);
        updateUI();
    }

    @Override
    public void onCardPlayed(Player player, Card card) {
        updateUI();
    }

    @Override
    public void onPlayerPassed(Player player) {
        updateUI();
    }

    @Override
    public void onRoundEnded(Player roundWinner, int p1Score, int p2Score) {
        playerHand.setDisable(true);
        playerHand.getChildren().clear();

        String winnerName = (roundWinner != null) ? roundWinner.getName() : "Draw";

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Round Over");
        alert.setHeaderText("Round " + gameService.getRound() + " finished");
        alert.setContentText("Round Winner: " + winnerName + "\n" +
                gameService.getPlayer1().getName() + ": " + p1Score +
                " (Wins: " + gameService.getPlayer1Wins() + ")\n" +
                gameService.getPlayer2().getName() + ": " + p2Score +
                " (Wins: " + gameService.getPlayer2Wins() + ")");
        alert.showAndWait();
    }

    @Override
    public void onGameEnded(Player gameWinner, int p1Wins, int p2Wins) {
        String winnerName = (gameWinner != null) ? gameWinner.getName() : "Draw - no clear winner";

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Game Over");
        alert.setHeaderText("Match Finished");
        alert.setContentText("Overall Winner: " + winnerName + "\n\n" +
                "Final Score:\n" +
                gameService.getPlayer1().getName() + ": " + p1Wins + " wins\n" +
                gameService.getPlayer2().getName() + ": " + p2Wins + " wins");
        alert.showAndWait();

        newRoundButton.setDisable(true);
        playerHand.setDisable(true);
        playerHand.getChildren().clear();
        restartGameButton.setVisible(true);
    }

    @Override
    public void onPlayerChanged(Player newCurrentPlayer) {
        updateUI();
    }
}
