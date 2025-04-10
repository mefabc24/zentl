package org.example.demo3;

import org.example.demo3.model.*;
import org.example.demo3.model.enums.*;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.geometry.*;
import java.net.URL;
import java.util.*;

public class HelloController implements Initializable {
    @FXML private HBox gameBoard;
    @FXML private VBox player1Side;
    @FXML private VBox player2Side;
    @FXML private HBox playerHand;
    @FXML private Label roundLabel;
    @FXML private Label currentPlayerLabel;
    @FXML private Label p1Score;
    @FXML private Label p2Score;
    @FXML private Button newRoundButton;
    @FXML private Button restartGameButton;

    private Player player1;
    private Player player2;
    private GameBoard gameBoardState;
    private Player currentPlayer;
    private int round = 1;

    private int player1Wins = 0;
    private int player2Wins = 0;
    private final int MAX_ROUNDS = 3;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        newRoundButton.setOnAction(e -> startNewRound());
        restartGameButton.setOnAction(e -> restartGame());  // Added event handler for restart
        restartGameButton.setVisible(false);  // Initially hide the restart game button
    }

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
        newRoundButton.setDisable(false);
        playerHand.setDisable(false);
        updateUI();
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

    private void updateUI() {
        updatePlayerSide(player1Side, player1);
        updatePlayerSide(player2Side, player2);
        updateHandUI();

        roundLabel.setText("Round: " + round);
        currentPlayerLabel.setText("Current: " + currentPlayer.getName());
        p1Score.setText(player1.getName() + " score: " + gameBoardState.calculateTotalPower(player1));
        p2Score.setText(player2.getName() + " score: " + gameBoardState.calculateTotalPower(player2));
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

        List<Card> cards = gameBoardState.getPlayerRows(player).getOrDefault(rowType, Collections.emptyList());
        for (Card card : cards) {
            VBox cardUI = createCardUI(card);
            cardsBox.getChildren().add(cardUI);
        }

        int rowPower = gameBoardState.calculateRowPower(rowType, player);
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

        // Do not show cards if current player has passed or both passed
        if (player1.hasPassed() && player2.hasPassed()) {
            playerHand.setDisable(true);
            return;
        }

        if (currentPlayer.hasPassed()) return;

        for (Card card : currentPlayer.getHand()) {
            VBox cardUI = createCardUI(card);
            cardUI.setOnMouseClicked(e -> {
                currentPlayer.playCard(card, gameBoardState);
                updateUI();
                switchPlayer();
            });
            playerHand.getChildren().add(cardUI);
        }

        Button passButton = new Button("Pass");
        passButton.setStyle("-fx-font-size: 14px; -fx-padding: 5px 15px;");
        passButton.setOnAction(e -> {
            currentPlayer.pass();
            updateUI();
            switchPlayer();
        });
        playerHand.getChildren().add(passButton);
    }

    private String getRowColor(RowType rowType) {
        switch (rowType) {
            case MELEE: return "#ffdddd";
            case RANGED: return "#ddffdd";
            case SIEGE: return "#ddddff";
            default: return "#ffffff";
        }
    }

    private void switchPlayer() {
        if (player1.hasPassed() && player2.hasPassed()) {
            playerHand.setDisable(true); // ⛔ Disable hand
            endRound();
        } else {
            currentPlayer = (currentPlayer == player1) ? player2 : player1;
            updateUI();
        }
    }

    private void endGame() {
        String gameWinner;
        if (player1Wins > player2Wins) {
            gameWinner = player1.getName();
        } else if (player2Wins > player1Wins) {
            gameWinner = player2.getName();
        } else {
            gameWinner = "Draw - no clear winner";
        }

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Game Over");
        alert.setHeaderText("Match Finished After " + MAX_ROUNDS + " Rounds");
        alert.setContentText("Overall Winner: " + gameWinner + "\n\n" +
                "Final Score:\n" +
                player1.getName() + ": " + player1Wins + " wins\n" +
                player2.getName() + ": " + player2Wins + " wins");
        alert.showAndWait();

        newRoundButton.setDisable(true);
        playerHand.setDisable(true);
        playerHand.getChildren().clear(); // Clear visual hand
        restartGameButton.setVisible(true); // Show the Restart Game button
    }

    private void endRound() {
        int p1Score = gameBoardState.calculateTotalPower(player1);
        int p2Score = gameBoardState.calculateTotalPower(player2);

        String roundWinner;
        if (p1Score > p2Score) {
            player1Wins++;
            roundWinner = player1.getName();
        } else if (p2Score > p1Score) {
            player2Wins++;
            roundWinner = player2.getName();
        } else {
            roundWinner = "Draw";
        }

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Round Over");
        alert.setHeaderText("Round " + round + " finished");
        alert.setContentText("Round Winner: " + roundWinner + "\n" +
                player1.getName() + ": " + p1Score + " (Wins: " + player1Wins + ")\n" +
                player2.getName() + ": " + p2Score + " (Wins: " + player2Wins + ")");
        alert.showAndWait();

        playerHand.getChildren().clear(); // Clear hand visually

        if (round >= MAX_ROUNDS) {
            endGame();
        }
    }

    private void startNewRound() {
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
        playerHand.setDisable(false);
        updateUI();
    }

    private void restartGame() {
        // Reset everything to initial state
        initializeGame();
        updateUI();
        newRoundButton.setDisable(false);
        playerHand.setDisable(false);
        restartGameButton.setVisible(false); // Hide the restart game button again
    }
}
