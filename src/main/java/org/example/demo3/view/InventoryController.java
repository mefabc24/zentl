package org.example.demo3.view;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import org.example.demo3.model.cards.Card;
import org.example.demo3.model.enums.Faction;
import org.example.demo3.model.logic.CardRepository;
import org.example.demo3.model.service.NavigationService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.example.demo3.model.constants.InventoryConstants.*;

public class InventoryController {

    @FXML private TabPane mainTabPane;
    @FXML private Button globalSaveButton;
    @FXML private Button backToMenuButton;

    private CardRepository cardRepository;
    private List<Card> allCardsMasterList;
    private ObservableList<Card> selectedCardsList;

    private NavigationService navigationService;

    public void setNavigationService(NavigationService navigationService) {
        this.navigationService = navigationService;
    }

    @FXML
    public void initialize() {
        System.out.println("--- Initializing Inventory UI (HelloController) and Loading Cards ---");

        selectedCardsList = FXCollections.observableArrayList();

        this.cardRepository = new CardRepository(JSON_PATH);
        List<Card> loadedCards = this.cardRepository.getAllCards();

        if (loadedCards == null || loadedCards.isEmpty()) {
            System.err.println("FEHLER: Keine Karten zum Anzeigen geladen im HelloController.");

            Tab errorTab = new Tab("Error");
            errorTab.setContent(new Label("Fehler: Keine Karten zum Anzeigen geladen."));
            if (mainTabPane != null) {
                mainTabPane.getTabs().add(errorTab);
            }
            return;
        }

        this.allCardsMasterList = new ArrayList<>(loadedCards);

        for (Card card : this.allCardsMasterList) {
            if (card.isSelected()) {
                if (!selectedCardsList.contains(card)) {
                    selectedCardsList.add(card);
                }
            }
        }

        createFactionTabs();

        if (globalSaveButton != null) {
            globalSaveButton.setOnAction(event -> handleSaveButtonAction());
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
                factionController.initializeData(faction, this.allCardsMasterList, this.selectedCardsList, this);

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
        if (this.cardRepository != null) {
            this.cardRepository.save();
            System.out.println("Aktueller Zustand aller Karten (isSelected, isUnlocked) gespeichert.");

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Speichern erfolgreich");
            alert.setHeaderText(null);
            alert.setContentText("Kartendaten wurden erfolgreich gespeichert!");
            alert.showAndWait();
        } else {
            System.err.println("Fehler: CardRepository ist nicht initialisiert. Speichern nicht möglich.");
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Speichern fehlgeschlagen");
            alert.setHeaderText(null);
            alert.setContentText("Kartendaten konnten nicht gespeichert werden (Repository nicht vorhanden).");
            alert.showAndWait();
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

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Navigationsfehler");
            alert.setHeaderText(null);
            alert.setContentText("Zurück zum Hauptmenü nicht möglich (NavigationService fehlt).");
            alert.showAndWait();
        }
    }

    public CardRepository getCardRepository() {
        return cardRepository;
    }

    public void showTemporaryMessage(String message, int durationMillis) {
        System.out.println("UI Message: " + message);
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}