package org.example.demo3.view;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import org.example.demo3.model.service.NavigationService;

import static org.example.demo3.model.constants.Config.FXML_DECKBUILDER_PATH;
import static org.example.demo3.model.constants.Config.FXML_MAINMENU_PATH;

public class DropSelectionController {
    @FXML private Button basicDropButton;
    @FXML private Button premiumDropButton;
    @FXML private Button eliteDropButton;
    @FXML private Button cancelDropButton;


    private NavigationService navigationService;

    public void setNavigationService(NavigationService navigationService) {
        this.navigationService = navigationService;
    }

    @FXML
    private void initialize() {
        basicDropButton.setOnAction(event -> {
            System.out.println("basicDropButton");
        });

        premiumDropButton.setOnAction(event -> {
            System.out.println("premiumDropButton");
        });

        eliteDropButton.setOnAction(event -> {
            System.out.println("eliteDropButton");
        });

        cancelDropButton.setOnAction(event -> {
            System.out.println("Cancel Button clicked");
            if (navigationService != null) {
                navigationService.navigateTo(FXML_DECKBUILDER_PATH, "Deckbuilder", (DeckbuilderController controller) ->
                        controller.setNavigationService(navigationService));
            } else {
                System.err.println("NavigationService not initialized in MainMenuController.");
            }
        });
    }

}
