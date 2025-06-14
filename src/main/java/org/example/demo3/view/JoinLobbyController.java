package org.example.demo3.view;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import org.example.demo3.model.service.NavigationService;
import org.example.demo3.network.Client;

import static org.example.demo3.model.constants.Config.FXML_GAME_SETUP_PATH;
import static org.example.demo3.model.constants.Config.FXML_LAN_SELECTION_PATH;

// controller for the client lobby screen => allows a player to join a game hosted by another player via ip
public class JoinLobbyController {

    @FXML
    private TextField ipTextField;
    @FXML
    private Label statusLabel;
    @FXML
    private Button connectButton;

    private NavigationService navigationService;
    private Client client;
    private volatile boolean isNavigating = false; // flag to prevent multiple navigations

    public void setNavigationService(NavigationService navigationService) {
        this.navigationService = navigationService;
    }

    // set text to localhost for quick connect
    @FXML
    public void initialize() {
        ipTextField.setText("localhost");
    }

    // handle connect button click
    @FXML
    private void handleConnectAction() {
        String ip = ipTextField.getText();

        // update ui to reflect connection attempt
        statusLabel.setText("Verbinde mit " + ip.trim() + "...");
        connectButton.setDisable(true);
        ipTextField.setDisable(true);

        // create new client
        client = new Client(ip.trim(), 50000,
                this::onMessageReceived, // sucess callback
                this::handleConnectionFailed // failure callback
        );

        Thread clientThread = new Thread(client);
        clientThread.setDaemon(true);
        clientThread.start();
    }

    // callback for sucessfull connection with server
    private void onMessageReceived(String message) {
        // check message from the host
        if ("CONNECTION_ACCEPTED".equals(message) && !isNavigating) {
            isNavigating = true; // prevent further navigation triggers
            Platform.runLater(() -> {
                statusLabel.setText("Erfolgreich verbunden");
                navigationService.navigateTo(FXML_GAME_SETUP_PATH, "Gwent - Game Setup", controller -> {
                    if (controller instanceof GameSetupController) {
                        // pass the client instance to the next screen
                        ((GameSetupController) controller).initLanData(navigationService, false, null, client);
                    }
                });
            });
        }
    }

    // callback for sucessfull connection with client
    private void handleConnectionFailed() {
        Platform.runLater(() -> {
            showAlert("Verbindung fehlgeschlagen", "Konnte keine Verbindung zum Host herstellen. Bitte IP-Adresse überprüfen");
            // reset ui to allow another connection attempt
            statusLabel.setText("Verbindung fehlgeschlagen");
            connectButton.setDisable(false);
            ipTextField.setDisable(false);
        });
    }

    @FXML
    private void handleBackAction() {
        // ensure the client connection is stopped if it exists
        if (client != null) {
            client.stop();
        }
        navigationService.navigateTo(FXML_LAN_SELECTION_PATH, "LAN Multiplayer", controller -> {
            if (controller instanceof LanSelectionController) {
                ((LanSelectionController) controller).setNavigationService(navigationService);
            }
        });
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}