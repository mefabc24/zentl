package org.example.demo3.view;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import org.example.demo3.model.service.NavigationService;
import org.example.demo3.network.Server;

import static org.example.demo3.model.constants.Config.FXML_GAME_SETUP_PATH;
import static org.example.demo3.model.constants.Config.FXML_LAN_SELECTION_PATH;

// controller for the host lobby => displays the host ip and waits for a client to connect
public class HostLobbyController {

    @FXML
    private Label ipLabel;
    @FXML
    private Label statusLabel;

    private NavigationService navigationService;
    private Server server;

    private volatile boolean isNavigating = false; // flag to prevent multiple navigations

    public void setNavigationService(NavigationService navigationService) {
        this.navigationService = navigationService;
    }

    @FXML
    public void initialize() {
        statusLabel.setText("Warte auf einen Mitspieler...");
        // try to get local ip address => display it
        try {
            String ip = java.net.InetAddress.getLocalHost().getHostAddress();
            ipLabel.setText("Deine IP-Adresse: " + ip);
        } catch (java.net.UnknownHostException e) {
            ipLabel.setText("IP-Adresse konnte nicht ermittelt werden.");
            statusLabel.setText("Netzwerkproblem");
            return;
        }

        // start server
        server = new Server(50000,
                this::onMessageReceived, // sucess callback
                this::handleServerStartupFailed); // failed callback
        new Thread(server).start();
    }

    // callback for sucessfull connecting with client
    private void onMessageReceived(String jsonMessage) {
        // UI updates must run on the javafx thread
        Platform.runLater(() -> {
            // ensure only navigating once
            if (isNavigating) {
                return;
            }
            isNavigating = true;

            if (server != null) {
                statusLabel.setText("Spieler verbunden! Gehe zum Setup...");

                // send a confirmation message to the client so they can navigate too
                server.sendMessage("CONNECTION_ACCEPTED");

                // navigate to the game setup screen as the host
                navigationService.navigateTo(FXML_GAME_SETUP_PATH, "Gwent - Host Lobby", controller -> {
                    if (controller instanceof GameSetupController) {
                        ((GameSetupController) controller).initLanData(navigationService, true, server, null);
                    }
                });
            }
        });
    }

    @FXML
    private void handleBackAction() {
        // ensure the server is stopped before going back
        if (server != null) {
            server.stop();
        }
        navigationService.navigateTo(FXML_LAN_SELECTION_PATH, "LAN Multiplayer", controller -> {
            if (controller instanceof LanSelectionController) {
                ((LanSelectionController) controller).setNavigationService(navigationService);
            }
        });
    }

    // callback for failed client connection
    private void handleServerStartupFailed() {
        Platform.runLater(() -> {
            statusLabel.setText("Fehler: Server konnte nicht gestartet werden (Port evtl. belegt?");
        });
    }
}