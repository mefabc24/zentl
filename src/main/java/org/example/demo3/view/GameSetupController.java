package org.example.demo3.view;

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

    private NavigationService navigationService;
    private CardRepository cardRepository;
    private GameMode gameMode;
    private BotDifficulty botDifficulty;

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

        p1FactionComboBox.valueProperty().addListener((obs, oldVal, newVal) -> updateDeckStatusLabels());
        p2FactionComboBox.valueProperty().addListener((obs, oldVal, newVal) -> updateDeckStatusLabels());

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
            p2DeckStatusLabel.setText("Bot will use a random deck");
        }
    }

    private void updateDeckStatusLabels() {
        updateStatusForPlayer(p1FactionComboBox.getValue(), p1DeckStatusLabel);
        if (gameMode == GameMode.PLAYER_VS_PLAYER) {
            updateStatusForPlayer(p2FactionComboBox.getValue(), p2DeckStatusLabel);
        }
    }

    private void updateStatusForPlayer(Faction faction, Label statusLabel) {
        if (faction == null) {
            statusLabel.setText("");
            return;
        }
        DeckStatus status = getDeckStatus(faction);
        statusLabel.setText(status.message);
        statusLabel.getStyleClass().remove("deck-status-warning");
        statusLabel.getStyleClass().remove("deck-status-ok");
        statusLabel.getStyleClass().add(status.isValid ? "deck-status-ok" : "deck-status-warning");
    }


    @FXML
    void handleStartGameAction(ActionEvent event) {
        Faction p1Faction = p1FactionComboBox.getValue();
        Faction p2Faction = p2FactionComboBox.getValue();

        if (p1Faction == null || (p2Faction == null && gameMode == GameMode.PLAYER_VS_PLAYER)) {
            showAlert("Selection Missing", "Please select a faction for both players.");
            return;
        }

        if (gameMode == GameMode.PLAYER_VS_PLAYER && p1Faction == p2Faction) {
            showAlert("Selection Invalid", "Players cannot choose the same faction.");
            return;
        }
        
        SoundService.getInstance().stopMenuMusic();
        
        if (navigationService != null) {
            navigationService.navigateTo(FXML_GAME_PATH, "Gwent", (GameController controller) -> {
                GameService gameService = new GameService();
                controller.setGameService(gameService);
                controller.setNavigationService(navigationService);
                gameService.newGame(gameMode, botDifficulty, p1Faction, p2Faction);
            });
        }
    }

    @FXML
    void handleBackAction(ActionEvent event) {
        if (navigationService != null) {
            navigationService.navigateTo(FXML_MAINMENU_PATH, "Main Menu", (MainMenuController controller) -> {
                controller.setNavigationService(navigationService);
            });
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
        final boolean isGameReady;
        final String message;
        DeckStatus(boolean isValid, boolean isGameReady, String message) {
            this.isValid = isValid;
            this.isGameReady = isGameReady;
            this.message = message;
        }
    }

    private DeckStatus getDeckStatus(Faction faction) {
        if (faction == null) {
            return new DeckStatus(false, false, "No faction selected");
        }
        List<Card> savedDeck = cardRepository.getSavedDeck(faction);
        if (!savedDeck.isEmpty()) {
            return new DeckStatus(true, true, String.format("✓ Deck ready (%d/%d)", savedDeck.size(), MIN_SELECTION));
        }
        long selectedCount = cardRepository.getAllCards().stream()
                .filter(c -> c.getFaction() == faction && c.getSelectedAmount() > 0 && c.isUnlocked())
                .mapToInt(Card::getSelectedAmount)
                .sum();

        if (selectedCount > 0) {
            String msg = String.format("⚠️ Deck incomplete (%d/%d)", selectedCount, MIN_SELECTION);
            return new DeckStatus(false, true, msg);
        } else {
            return new DeckStatus(false, true, "⚠️ No deck selected");
        }
    }
}