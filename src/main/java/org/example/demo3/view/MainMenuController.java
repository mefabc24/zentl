package org.example.demo3.view;

import javafx.fxml.FXML;
import javafx.stage.Stage;
import org.example.demo3.model.service.NavigationService;

import static org.example.demo3.model.constants.Config.*;

public class MainMenuController {

    private Stage primaryStage;
    private NavigationService navigationService;

    public void setStage(Stage stage) {
        this.primaryStage = stage;
    }

    public void setNavigationService(NavigationService navigationService) {
        this.navigationService = navigationService;
    }

    @FXML
    private void handlePlayButtonAction() {
        System.out.println("Play button clicked - Navigating to Game Mode Selection");
        if (navigationService != null) {
            // Navigiere zum neuen Game Mode Selection Screen
            navigationService.navigateTo(FXML_GAME_MODE_SELECTION_PATH, "Gwent - Select Mode", (GameModeSelectionController controller) ->
                controller.setNavigationService(navigationService));
        } else {
            System.err.println("NavigationService not initialized in MainMenuController.");
        }
    }
    
    @FXML
    private void handleInventoryButtonAction() {
        System.out.println("Inventory button clicked");
        if (navigationService != null) {
            navigationService.navigateTo(FXML_INVENTORY_PATH, "Card Inventory", (InventoryController controller) ->
                controller.setNavigationService(navigationService));
        } else {
            System.err.println("NavigationService not initialized in MainMenuController.");
        }
    }

    @FXML
    private void handleEditorButtonAction() {
        System.out.println("Editor button clicked.");
        if (navigationService != null) {
            navigationService.navigateTo(FXML_EDITOR_PATH, "Editor", (EditorController controller) ->
                controller.setNavigationService(navigationService));
        } else {
            System.err.println("NavigationService not initialized in MainMenuController.");
        }
    }

    @FXML
    private void handleQuitButtonAction() {
        if (navigationService != null) {
            navigationService.navigateTo(FXML_UNLOCKER_PATH, "Unlocker", (CardUnlockController controller) ->
                    controller.setNavigationService(navigationService));
        } else {
            System.err.println("NavigationService not initialized in MainMenuController.");
        }
    }

    /*
    @FXML
    private void handleQuitButtonAction() {
        System.out.println("Quit button clicked");
        Platform.exit();
    }*/
}