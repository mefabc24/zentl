package org.example.demo3.view;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.example.demo3.model.Toast;
import org.example.demo3.model.cards.Card;
import org.example.demo3.model.cards.CardInstance;
import org.example.demo3.model.enums.Faction;
import org.example.demo3.model.logic.CardRepository;
import org.example.demo3.model.service.NavigationService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.example.demo3.model.constants.Config.*;

public class InventoryController {

    @FXML private TabPane mainTabPane;
    @FXML private Button globalSaveButton;
    @FXML private Button backToMenuButton;

    private CardRepository cardRepository;
    private List<Card> allCardsMasterList;
    private ObservableList<CardInstance> selectedCardInstanceList;

    public ObservableList<CardInstance> getSelectedCardInstanceList() {
        return selectedCardInstanceList;
    }

    private NavigationService navigationService;

    public void setNavigationService(NavigationService navigationService) {
        this.navigationService = navigationService;
    }

    @FXML
    public void initialize() {
        selectedCardInstanceList  = FXCollections.observableArrayList();
        cardRepository = new CardRepository();

        loadCards();
        createFactionTabs();
        setupButtonActions();
    }

    private void loadCards() {
        List<Card> loadedPrototypes = this.cardRepository.getAllCards();
        if (loadedPrototypes == null || loadedPrototypes.isEmpty()) {
            showErrorTab("Fehler: Keine Karten zum Anzeigen geladen.");
            return;
        }
        this.allCardsMasterList = new ArrayList<>(loadedPrototypes);
        for (Card prototype : allCardsMasterList) {
            if (prototype.isUnlocked()) {
                int maxInstances = Math.min(prototype.getSelectedAmount(), prototype.getMaxAmount());
                for (int i = 0; i < maxInstances; i++) {
                    selectedCardInstanceList.add(new CardInstance(prototype));
                }
                if (prototype.getSelectedAmount() > prototype.getMaxAmount()) {
                    System.out.println("Warnung: selectedAmount für Karte " + prototype.getName() +
                            " überschreitet maxAmount. Nur maxAmount Instanzen werden geladen.");
                }
            } else {
                prototype.setSelectedAmount(0);
            }
        }
    }

    private void setupButtonActions() {
        if (globalSaveButton != null) {
            globalSaveButton.setOnAction(event -> handleSaveButtonAction());
        }
        if (backToMenuButton != null) {
            backToMenuButton.setOnAction(this::handleBackToMenuButtonAction);
        }
    }

    private void showErrorTab(String message) {
        if (mainTabPane != null) {
            Tab errorTab = new Tab("Error");
            errorTab.setContent(new Label(message));
            mainTabPane.getTabs().add(errorTab);
        }
    }

    public void createFactionTabs() {
        if (mainTabPane == null) {
            System.err.println("FEHLER: mainTabPane ist null in HelloController. Tabs können nicht erstellt werden.");
            return;
        }
        mainTabPane.getTabs().clear();

        for (Faction faction : Faction.values()) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource(FXML_FACTION_TAB_PATH));
                Parent tabContentRoot = loader.load();

                FactionTabController factionController = loader.getController();
                factionController.initializeData(faction, this.allCardsMasterList, this.selectedCardInstanceList, this);

                Tab factionTab = new Tab(faction.name());
                factionTab.setContent(tabContentRoot);
                mainTabPane.getTabs().add(factionTab);

            } catch (IOException e) {
                System.err.println("Fehler beim Laden des Tab-Inhalts für Fraktion " + faction.name() + ": " + e.getMessage());
                e.printStackTrace();
                Tab errorTab = new Tab(faction.name() + " (Error)");
                errorTab.setContent(new Label("Konnte Inhalt für " + faction.name() + " nicht laden."));
                mainTabPane.getTabs().add(errorTab);
            }
        }
    }

    @FXML
    private void handleSaveButtonAction() {
        if (this.cardRepository != null && this.allCardsMasterList != null) {

            for (Card prototype : this.allCardsMasterList) {
                long count = selectedCardInstanceList.stream()
                        .filter(instance -> instance.getCardDefinition().equals(prototype))
                        .count();
                prototype.setSelectedAmount((int) count);
            }

            this.cardRepository.save();
            System.out.println("Aktueller Zustand aller Karten (inkl. selectedAmount) gespeichert.");

            showToast("Kartendaten erfolgreich gespeichert", 2000);
        } else {
            showToast("Speichern fehlgeschlagen", 200);
        }
    }

    @FXML
    private void handleBackToMenuButtonAction(ActionEvent event) {
        System.out.println("Back to Main Menu button clicked in HelloController.");
        if (navigationService != null) {
            navigationService.navigateTo(FXML_MAINMENU_PATH, "My Awesome Card Game - Main Menu", (MainMenuController controller) -> {
                controller.setNavigationService(navigationService);
            });
        } else {
            System.err.println("NavigationService ist nicht im HelloController initialisiert. Kann nicht navigieren.");
            showToast("Fehler bei der Navigation", 2000);
        }
    }

    public CardRepository getCardRepository() {
        return cardRepository;
    }

    public void showToast(String message, int duration) {
        if (mainTabPane != null && mainTabPane.getScene() != null) {
            Stage stage = (Stage) mainTabPane.getScene().getWindow();
            Toast.makeText(stage, message, duration);
        } else {
            System.err.println("Kann Toast nicht anzeigen: mainTabPane oder Scene ist null.");
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        System.out.println("UI Message: " + message);
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}