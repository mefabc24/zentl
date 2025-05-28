package org.example.demo3.view;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import org.example.demo3.model.service.GameService;
import org.example.demo3.model.service.NavigationService;

import static org.example.demo3.model.constants.InventoryConstants.*;


public class MainMenuController {

    @FXML private Button playButton;
    @FXML private Button inventoryButton;
    @FXML private Button editorButton;
    @FXML private Button quitButton;

    private Stage primaryStage;
    private NavigationService navigationService; // Referenz zum NavigationService

    public void setStage(Stage stage) {
        this.primaryStage = stage;
    }

    // Neue Methode, um den NavigationService zu setzen
    public void setNavigationService(NavigationService navigationService) {
        this.navigationService = navigationService;
    }

    @FXML
    private void handlePlayButtonAction(ActionEvent event) {
        System.out.println("Play button clicked - Not implemented yet");
        if (navigationService != null) {
            navigationService.navigateTo(FXML_GAME_PATH, "Gwent", (GameController controller) -> {
                controller.setNavigationService(navigationService);

                GameService gameService = new GameService();
                if (gameService != null) {
                    System.out.println("MainMenuController: Requesting new game from GameService.");
                    gameService.newGame();
                } else {
                    System.err.println("GameService not available in HelloApplication when trying to start a new game.");
                }
            });
        } else {
            System.err.println("NavigationService not initialized in GameController.");
        }
    }

    @FXML
    private void handleInventoryButtonAction(ActionEvent event) {
        System.out.println("Inventory button clicked");
        if (navigationService != null) {
            navigationService.navigateTo(FXML_INVENTORY_PATH, "Card Inventory", (InventoryController controller) -> {
                controller.setNavigationService(navigationService);

            });
        } else {
            System.err.println("NavigationService not initialized in MainMenuController.");
        }
    }

    @FXML
    private void handleEditorButtonAction(ActionEvent event) {
        System.out.println("Editor button clicked - Not implemented yet");

        if (navigationService != null) {
            navigationService.navigateTo(FXML_EDITOR_PATH, "Editor", (EditorController controller) -> {
                controller.setNavigationService(navigationService);
            });
        } else {
            System.err.println("NavigationService not initialized in MainMenuController.");
        }

    }

    @FXML
    private void handleQuitButtonAction(ActionEvent event) {
        System.out.println("Quit button clicked");
        Platform.exit();
    }
}