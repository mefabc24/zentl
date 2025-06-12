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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
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
import java.util.*;

import static org.example.demo3.model.constants.Config.FXML_MAINMENU_PATH;

public class GameController implements Initializable, Cleanable {
    private final EventBus eventBus = EventBus.getInstanz();

    //<editor-fold desc="FXML Fields">
    @FXML private StackPane rootPane;
    @FXML private VBox player1Side, player2Side, weatherArea;
    @FXML private HBox playerHand;
    @FXML private Label p1NameLabel, p1ScoreLabel, p2NameLabel, p2ScoreLabel, roundLabel;
    @FXML private Button restartGameButton, backToMenuButton, passButton;
    @FXML private Label currentPlayerHandLabel; // Das neue Label für die Hand-Info
    //</editor-fold>

    //<editor-fold desc="Game State Fields">
    private Player p1, p2, currentPlayer;
    private String localPlayerName;
    private Player localPlayer;
    private Board gameBoard;
    private boolean gameEnded = false;
    private GameMode gameMode;
    private BotDifficulty botDifficulty;
    private NavigationService navigationService;
    private GameService gameService;
    //</editor-fold>

    //<editor-fold desc="Image & Asset Fields">
    private static final String UI_PATH = "/org/example/demo3/assets/UI_Components/table/";
    private static final String CARDS_ASSETS_PATH = "/org/example/demo3/assets/";
    private static final String DEFAULT_CARD_IMAGE_NAME = "default.png";
    private static final String HIDDEN_CARD_IMAGE_NAME = "hidden.png";
    private static final double CARD_WIDTH = 80;
    private static final double CARD_HEIGHT = 110;
    private final Map<String, Image> imageCache = new HashMap<>();
    //</editor-fold>

    //<editor-fold desc="Event Handlers">
    private final EventHandler<GameStateUpdateEve> gameStateUpdateHandler = event -> Platform.runLater(() -> updateGameState(event));
    private final EventHandler<RoundEndedEve> roundEndedHandler = event -> Platform.runLater(() -> displayRoundResult(event));
    private final EventHandler<GameEndedEve> gameEndedHandler = event -> Platform.runLater(() -> displayGameOver(event));
    private final EventHandler<EffectLogEvent> effectLogHandler = event -> Platform.runLater(() -> showEffectLog(event.getMessage()));
    //</editor-fold>

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        subscribeToEvents();
        restartGameButton.setVisible(false);
        currentPlayerHandLabel.setVisible(false); // Initial ausblenden
    }

    //<editor-fold desc="Setup & Cleanup">
    public void setNavigationService(NavigationService navigationService) { this.navigationService = navigationService; }
    public void setGameService(GameService gameService) { this.gameService = gameService; }
    public void setLocalPlayerIdentity(String name) { this.localPlayerName = name; }

    private void subscribeToEvents() {
        eventBus.subscribe(GameStateUpdateEve.class, gameStateUpdateHandler);
        eventBus.subscribe(RoundEndedEve.class, roundEndedHandler);
        eventBus.subscribe(GameEndedEve.class, gameEndedHandler);
        eventBus.subscribe(EffectLogEvent.class, effectLogHandler);
    }

    @Override
    public void cleanup() {
        System.out.println("GameController cleaning up...");
        if (gameService != null) {
            gameService.shutdownNetwork();
            gameService.shutdown();
        }
        eventBus.unsubscribe(GameStateUpdateEve.class, gameStateUpdateHandler);
        eventBus.unsubscribe(RoundEndedEve.class, roundEndedHandler);
        eventBus.unsubscribe(GameEndedEve.class, gameEndedHandler);
        eventBus.unsubscribe(EffectLogEvent.class, effectLogHandler);
    }
    //</editor-fold>

    //<editor-fold desc="FXML Actions">
    @FXML void handleBackToMenu(ActionEvent event) {
        cleanup();
        SoundService.getInstance().startMenuMusic();
        if (navigationService != null) {
            navigationService.navigateTo(FXML_MAINMENU_PATH, "GWENT", c -> {
                if (c instanceof MainMenuController) ((MainMenuController) c).setNavigationService(navigationService);
            });
        }
    }

    @FXML void handleRestartGame(ActionEvent event) {
        cleanup();
        subscribeToEvents();
        gameEnded = false;
        this.gameService = new GameService();
        gameService.newGame(gameMode, botDifficulty, p1.getfraction(), p2.getfraction());
    }

    @FXML void handlePassAction(ActionEvent event) {
        eventBus.post(new PlayerPassed());
    }
    //</editor-fold>

    private void updateGameState(GameStateUpdateEve event) {
        if (this.gameMode == null) {
            this.gameMode = event.getGameMode();
            this.botDifficulty = event.getBotDifficulty();
        }
        this.p1 = event.getPlayer1();
        this.p2 = event.getPlayer2();
        this.currentPlayer = event.getCurrentPlayer();
        this.gameBoard = event.getGameBoard();

        if (localPlayerName != null) {
            localPlayer = (p1 != null && p1.getName().equals(localPlayerName)) ? p1 : p2;
        }

        if (gameEnded) return;

        restartGameButton.setVisible(false);
        updateUI(event.getRound());
    }

    private void updateUI(int round) {
        if (p1 == null || p2 == null || currentPlayer == null) return;

        // Update top info bar
        p1NameLabel.setText(p1.getName());
        p1ScoreLabel.setText(String.valueOf(p1.getScore()));
        p2NameLabel.setText(p2.getName());
        p2ScoreLabel.setText(String.valueOf(p2.getScore()));
        roundLabel.setText("RUNDE " + round);

        // Update Board
        updatePlayerSide(player1Side, p1);
        updatePlayerSide(player2Side, p2);
        updateWeatherArea();
        updatePlayerHand();
    }

    private void updatePlayerSide(VBox playerSide, Player player) {
        playerSide.getChildren().clear();
        for (RowType rowType : List.of(RowType.MELEE, RowType.RANGED, RowType.SIEGE)) {
            playerSide.getChildren().add(createFullRow(rowType, player));
        }
    }

    private Node createFullRow(RowType rowType, Player player) {
        HBox container = new HBox();
        container.getStyleClass().add("full-row-container");
        container.setMaxWidth(Double.MAX_VALUE); // Allows the HBox to grow to the full width of its parent VBox.

        int power = gameBoard.calculateRowPower(rowType, player);
        Label scoreLabel = new Label(String.valueOf(power));
        scoreLabel.getStyleClass().add("row-score-label");

        StackPane rowBox = createCardRowBox(rowType, player);
        HBox.setHgrow(rowBox, Priority.ALWAYS); // Tells the container HBox to give all extra space to the rowBox.

        if (player == p1) {
            container.getChildren().addAll(scoreLabel, rowBox);
        } else {
            container.getChildren().addAll(rowBox, scoreLabel);
        }
        return container;
    }

    private StackPane createCardRowBox(RowType rowType, Player player) {
        StackPane rowBox = new StackPane();
        rowBox.getStyleClass().add("game-row-box");
        List<Card> cards = gameBoard.getPlayerRows(player).getOrDefault(rowType, Collections.emptyList());

        if (cards.isEmpty()) {
            String iconName = switch (rowType) {
                case MELEE -> "melee.png";
                case RANGED -> "ranged.png";
                case SIEGE -> "siege.png";
                default -> null;
            };

            if (iconName != null) {
                ImageView icon = new ImageView(loadImage(UI_PATH + iconName));
                icon.setFitHeight(60);
                icon.setPreserveRatio(true);
                icon.setOpacity(0.5);
                rowBox.getChildren().add(icon);
            }
        } else {
            HBox cardsBox = new HBox(5);
            cardsBox.setAlignment(Pos.CENTER);
            for (Card card : cards) {
                cardsBox.getChildren().add(createCardUI(card, false));
            }
            rowBox.getChildren().add(cardsBox);
        }
        return rowBox;
    }

    private void updateWeatherArea() {
        weatherArea.getChildren().clear();
        StackPane weatherBox = new StackPane();
        weatherBox.getStyleClass().add("weather-box");

        List<Card> activeWeather = gameBoard.getActiveWeatherCards();
        if (activeWeather.isEmpty()) {
            ImageView icon = new ImageView(loadImage(UI_PATH + "effect.png"));
            icon.setFitHeight(60);
            icon.setPreserveRatio(true);
            icon.setOpacity(0.5);
            weatherBox.getChildren().add(icon);
        } else {
            HBox cardsBox = new HBox(5);
            cardsBox.setAlignment(Pos.CENTER);
            for (Card card : activeWeather) {
                cardsBox.getChildren().add(createCardUI(card, false));
            }
            weatherBox.getChildren().add(cardsBox);
        }
        weatherArea.getChildren().add(weatherBox);
    }

    private void updatePlayerHand() {
        playerHand.getChildren().clear();
        if (currentPlayer == null) {
            passButton.setDisable(true);
            currentPlayerHandLabel.setVisible(false); // Label ausblenden
            return;
        }

        List<Card> handToShow = null;
        boolean canPlay = false;
        String labelText = "";
        boolean showLabel = true;

        if (gameMode == GameMode.PLAYER_VS_BOT) {
            if (currentPlayer == p1) {
                handToShow = p1.getHand();
                canPlay = !p1.hasPassed();
                labelText = "Am Zug: " + p1.getName();
            } else {
                for (int i = 0; i < p2.getHand().size(); i++) {
                    playerHand.getChildren().add(createCardBackUI());
                }
                passButton.setDisable(true);
                labelText = "Gegner ist am Zug...";
            }
        } else if (gameMode == GameMode.BOT_VS_BOT || (gameMode == GameMode.PLAYER_VS_PLAYER && !gameService.isLanGame())) {
            handToShow = currentPlayer.getHand();
            canPlay = gameMode == GameMode.PLAYER_VS_PLAYER && !currentPlayer.hasPassed();
            labelText = "Am Zug: " + currentPlayer.getName();
        } else if (gameMode == GameMode.PLAYER_VS_PLAYER && gameService.isLanGame()) {
            if (localPlayer != null) {
                handToShow = localPlayer.getHand();
                canPlay = localPlayer.equals(currentPlayer) && !localPlayer.hasPassed();
                if (canPlay) {
                    labelText = "Du bist am Zug: " + localPlayer.getName();
                } else {
                    labelText = "Gegner ist am Zug...";
                }
            } else {
                showLabel = false; // Noch keine Infos
            }
        }

        // Label-Text und Sichtbarkeit setzen
        currentPlayerHandLabel.setText(labelText);
        currentPlayerHandLabel.setVisible(showLabel);

        // Pass-Button und Handkarten aktualisieren
        passButton.setDisable(!canPlay);
        if (handToShow != null) {
            for (Card card : handToShow) {
                playerHand.getChildren().add(createCardUI(card, canPlay));
            }
        }
    }


    private Node createCardUI(Card card, boolean clickable) {
        String specificImageName = card.getName() + ".png";
        Image image = loadImage(CARDS_ASSETS_PATH + specificImageName);
        if (image == null || image.isError()) {
            image = loadImage(CARDS_ASSETS_PATH + DEFAULT_CARD_IMAGE_NAME);
        }

        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(CARD_WIDTH);
        imageView.setFitHeight(CARD_HEIGHT);
        imageView.setPreserveRatio(true);
        imageView.getStyleClass().add("game-card-image");

        if (clickable) {
            imageView.setOnMouseClicked(e -> eventBus.post(new PlayCardRequest(card)));
        }
        return imageView;
    }

    private Node createCardBackUI() {
        Image hiddenImage = loadImage(CARDS_ASSETS_PATH + HIDDEN_CARD_IMAGE_NAME);
        ImageView imageView = new ImageView(hiddenImage);
        imageView.setFitWidth(CARD_WIDTH);
        imageView.setFitHeight(CARD_HEIGHT);
        imageView.setPreserveRatio(true);
        imageView.getStyleClass().add("game-card-image");
        return imageView;
    }

    private Image loadImage(String imagePath) {
        if (imageCache.containsKey(imagePath)) return imageCache.get(imagePath);
        try {
            URL imageUrl = getClass().getResource(imagePath);
            if (imageUrl != null) {
                Image image = new Image(imageUrl.toExternalForm());
                if (!image.isError()) {
                    imageCache.put(imagePath, image);
                    return image;
                }
            }
        } catch (Exception e) { System.err.println("Could not load image: " + imagePath); }
        return null;
    }

    private void showEffectLog(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Karteneffekt");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.show();
    }

    private void displayRoundResult(RoundEndedEve event) {
        passButton.setDisable(true);
        String winner = (event.getWinner() != null) ? event.getWinner().getName() : "Unentschieden";
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Runde vorbei");
        alert.setHeaderText("Runde " + event.getRound() + " ist vorbei!");
        alert.setContentText("Gewinner: " + winner);
        alert.showAndWait();
    }

    private void displayGameOver(GameEndedEve event) {
        gameEnded = true;
        if (gameService != null) gameService.shutdown();
        String winner = (event.getWinner() != null) ? event.getWinner().getName() : "Unentschieden";
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Spiel vorbei");
        alert.setHeaderText("DAS SPIEL IST VORBEI!");
        alert.setContentText("Endgültiger Gewinner: " + winner);
        alert.showAndWait();
        passButton.setDisable(true);
        playerHand.getChildren().clear();
        restartGameButton.setVisible(true);
        currentPlayerHandLabel.setVisible(false); // Label am Ende des Spiels ausblenden
    }
}