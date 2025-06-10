package org.example.demo3.view;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.VBox;
import org.example.demo3.model.enums.BotDifficulty;
import org.example.demo3.model.enums.GameMode;
import org.example.demo3.model.service.NavigationService;

import static org.example.demo3.model.constants.Config.FXML_GAME_SETUP_PATH;
import static org.example.demo3.model.constants.Config.FXML_MAINMENU_PATH;

public class GameModeSelectionController {

    @FXML private Button pvpButton;
    @FXML private Button pvbButton;
    @FXML private VBox botSelectionPane;
    @FXML private RadioButton easyBotRadio;
    @FXML private RadioButton advancedBotRadio;
    @FXML private ToggleGroup botDifficultyToggleGroup;
    @FXML private Button startButton;

    private NavigationService navigationService;
    private GameMode selectedMode;

    public void setNavigationService(NavigationService navigationService) {
        this.navigationService = navigationService;
    }

    @FXML
    public void initialize() {
        // Event Listener für die Radio-Buttons, um den Start-Button zu aktivieren
        botDifficultyToggleGroup.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
            startButton.setDisable(newToggle == null);
        });
    }

    @FXML
    private void handlePvpButton() {
        selectedMode = GameMode.PLAYER_VS_PLAYER;
        botSelectionPane.setVisible(false);
        botSelectionPane.setManaged(false);
        startButton.setDisable(false);
        // Optional: Direkt zum nächsten Screen
        // handleStartGame(); 
    }

    @FXML
    private void handlePvbButton() {
        selectedMode = GameMode.PLAYER_VS_BOT;
        botSelectionPane.setVisible(true);
        botSelectionPane.setManaged(true);
        // Start-Button deaktivieren, bis eine Schwierigkeit gewählt ist
        startButton.setDisable(botDifficultyToggleGroup.getSelectedToggle() == null);
        if (botDifficultyToggleGroup.getSelectedToggle() == null) {
            easyBotRadio.setSelected(true); // Standardauswahl
        }
    }

    @FXML
    private void handleStartGame() {
        if (selectedMode == null) return;

        BotDifficulty difficulty = BotDifficulty.NONE;
        if (selectedMode == GameMode.PLAYER_VS_BOT) {
            if (easyBotRadio.isSelected()) {
                difficulty = BotDifficulty.EASY;
            } else if (advancedBotRadio.isSelected()) {
                difficulty = BotDifficulty.ADVANCED;
            }
        }

        // Navigiere zum Game Setup und übergebe die Auswahl
        final BotDifficulty finalDifficulty = difficulty;
        navigationService.navigateTo(FXML_GAME_SETUP_PATH, "Gwent - Game Setup", (GameSetupController controller) -> {
            controller.setNavigationService(navigationService);
            controller.initData(selectedMode, finalDifficulty);
        });
    }

    @FXML
    private void handleBackButton() {
        navigationService.navigateTo(FXML_MAINMENU_PATH, "GWENT", (MainMenuController controller) -> {
            controller.setNavigationService(navigationService);
        });
    }
}