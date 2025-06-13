package org.example.demo3.view;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.example.demo3.model.service.NavigationService;

import java.net.URL;
import java.util.ResourceBundle;

import static org.example.demo3.model.constants.Config.*;

public class MainMenuController implements Initializable {

    @FXML
    private StackPane rootPane;

    private Stage primaryStage;
    private NavigationService navigationService;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        //rootPane.getStyleClass().add("main-menu-root");
    }

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
            navigationService.navigateTo(FXML_DECKBUILDER_PATH, "Card Inventory", (DeckbuilderController controller) ->
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
        System.out.println("Quit button clicked");
        Platform.exit();
    }
}