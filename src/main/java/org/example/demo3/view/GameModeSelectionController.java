package org.example.demo3.view;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.example.demo3.model.enums.BotDifficulty;
import org.example.demo3.model.enums.GameMode;
import org.example.demo3.model.service.NavigationService;

import static org.example.demo3.model.constants.Config.*;

public class GameModeSelectionController {

    private NavigationService navigationService;
    private GameMode selectedMode;

    // Mode Buttons
    @FXML private Button pvpButton;
    @FXML private Button lanButton;
    @FXML private Button pvbButton;
    @FXML private Button bvbButton;

    // Continue/Back Buttons
    @FXML private HBox navigationButtonsBox;
    @FXML private Button continueButton; // The "Continue" button

    // Player vs Bot Pane
    @FXML private VBox botSelectionPane;
    @FXML private RadioButton easyBotRadio;
    @FXML private ToggleGroup botDifficultyToggleGroup;

    // Bot vs Bot Pane
    @FXML private VBox bvbSelectionPane;
    @FXML private ToggleGroup bot1DifficultyToggleGroup;
    @FXML private ToggleGroup bot2DifficultyToggleGroup;
    @FXML private RadioButton b1EasyRadio;
    @FXML private RadioButton b2EasyRadio;

    public void setNavigationService(NavigationService navigationService) {
        this.navigationService = navigationService;
    }

    @FXML
    public void initialize() {
        // Listeners now just update the UI state
        botDifficultyToggleGroup.selectedToggleProperty().addListener((obs, ov, nv) -> updateUiState());
        bot1DifficultyToggleGroup.selectedToggleProperty().addListener((obs, ov, nv) -> updateUiState());
        bot2DifficultyToggleGroup.selectedToggleProperty().addListener((obs, ov, nv) -> updateUiState());

        this.selectedMode = null;
        updateUiState();
    }

    @FXML private void handlePvpButton() {
        selectedMode = GameMode.PLAYER_VS_PLAYER;
        // For PvP, navigate immediately.
        handleContinueAction();
    }

    @FXML private void handleLanButton() {
        selectedMode = GameMode.LAN;
        updateUiState();
    }

    @FXML private void handlePvbButton() {
        selectedMode = GameMode.PLAYER_VS_BOT;
        updateUiState();
    }

    @FXML private void handleBvbButton() {
        selectedMode = GameMode.BOT_VS_BOT;
        updateUiState();
    }

    private void updateUiState() {
        // Reset styles and panes
        pvpButton.getStyleClass().remove("selected-mode-button");
        lanButton.getStyleClass().remove("selected-mode-button");
        pvbButton.getStyleClass().remove("selected-mode-button");
        bvbButton.getStyleClass().remove("selected-mode-button");

        botSelectionPane.setVisible(false);
        botSelectionPane.setManaged(false);
        bvbSelectionPane.setVisible(false);
        bvbSelectionPane.setManaged(false);

        continueButton.setVisible(false);
        continueButton.setManaged(false);

        if (selectedMode == null) return;

        // Configure UI based on the selected mode
        switch (selectedMode) {
            case LAN:
                lanButton.getStyleClass().add("selected-mode-button");
                // No continue button for LAN
                break;
            case PLAYER_VS_PLAYER:
                pvpButton.getStyleClass().add("selected-mode-button");
                // No continue button needed, as it navigates instantly
                break;
            case PLAYER_VS_BOT:
                pvbButton.getStyleClass().add("selected-mode-button");
                botSelectionPane.setVisible(true);
                botSelectionPane.setManaged(true);
                // Show continue button and enable it only if difficulty is chosen
                continueButton.setVisible(true);
                continueButton.setManaged(true);
                continueButton.setDisable(botDifficultyToggleGroup.getSelectedToggle() == null);
                break;
            case BOT_VS_BOT:
                bvbButton.getStyleClass().add("selected-mode-button");
                bvbSelectionPane.setVisible(true);
                bvbSelectionPane.setManaged(true);
                // Show continue button and enable it only if both difficulties are chosen
                continueButton.setVisible(true);
                continueButton.setManaged(true);
                continueButton.setDisable(bot1DifficultyToggleGroup.getSelectedToggle() == null ||
                        bot2DifficultyToggleGroup.getSelectedToggle() == null);
                break;
        }
    }

    @FXML
    private void handleContinueAction() {
        if (selectedMode == null || selectedMode == GameMode.LAN) return;

        BotDifficulty bot1Difficulty = BotDifficulty.NONE;
        BotDifficulty bot2Difficulty = BotDifficulty.NONE;

        if (selectedMode == GameMode.PLAYER_VS_BOT) {
            if (botDifficultyToggleGroup.getSelectedToggle() == null) return; // Should be disabled, but for safety
            bot1Difficulty = easyBotRadio.isSelected() ? BotDifficulty.EASY : BotDifficulty.ADVANCED;
        } else if (selectedMode == GameMode.BOT_VS_BOT) {
            if (bot1DifficultyToggleGroup.getSelectedToggle() == null || bot2DifficultyToggleGroup.getSelectedToggle() == null) return;
            bot1Difficulty = b1EasyRadio.isSelected() ? BotDifficulty.EASY : BotDifficulty.ADVANCED;
            bot2Difficulty = b2EasyRadio.isSelected() ? BotDifficulty.EASY : BotDifficulty.ADVANCED;
        }

        // Final navigation logic
        final BotDifficulty finalB1Diff = bot1Difficulty;
        final BotDifficulty finalB2Diff = bot2Difficulty;

        navigationService.navigateTo(FXML_GAME_SETUP_PATH, "Gwent - Game Setup", (GameSetupController controller) -> {
            controller.setNavigationService(navigationService);
            controller.initData(selectedMode, finalB1Diff, finalB2Diff);
        });
    }

    @FXML
    private void handleBackButton() {
        navigationService.navigateTo(FXML_MAINMENU_PATH, "GWENT", (MainMenuController controller) -> {
            controller.setNavigationService(navigationService);
        });
    }

    // navigate to lan selection (host or join)
    @FXML
    private void handleLanButtonAction() {
        navigationService.navigateTo(FXML_LAN_SELECTION_PATH, "LAN Multiplayer", controller -> {
            if (controller instanceof LanSelectionController) {
                ((LanSelectionController) controller).setNavigationService(navigationService);
            }
        });
    }
}