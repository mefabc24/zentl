package org.example.demo3.view;

import javafx.fxml.FXML;
import org.example.demo3.model.service.NavigationService;

import static org.example.demo3.model.constants.Config.*;

// controller for the lan multiplayer selection screen => allows the user to choose between hosting or joining a game
public class LanSelectionController {
    private NavigationService navigationService;

    public void setNavigationService(NavigationService navigationService) {
        this.navigationService = navigationService;
    }

    // navigates to host lobby screen
    @FXML
    private void handleHostAction() {
        navigationService.navigateTo(FXML_HOST_LOBBY_PATH, "Host Lobby", controller -> {
            if (controller instanceof HostLobbyController) {
                ((HostLobbyController) controller).setNavigationService(navigationService);
            }
        });
    }

    // navigates to join lobby
    @FXML
    private void handleJoinAction() {
        navigationService.navigateTo(FXML_JOIN_LOBBY_PATH, "Join Lobby", controller -> {
            if (controller instanceof JoinLobbyController) {
                ((JoinLobbyController) controller).setNavigationService(navigationService);
            }
        });
    }

    // back to the gamemode selection screen
    @FXML
    private void handleBackAction() {
        navigationService.navigateTo(FXML_GAME_MODE_SELECTION_PATH, "Gwent - Select Mode", controller -> {
            if (controller instanceof GameModeSelectionController) {
                ((GameModeSelectionController) controller).setNavigationService(navigationService);
            }
        });
    }
}