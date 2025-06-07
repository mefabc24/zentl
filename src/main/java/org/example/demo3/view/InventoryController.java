package org.example.demo3.view;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.example.demo3.model.effect.Toast;
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
    private NavigationService navigationService;
    private boolean isInitializing = true;

    public ObservableList<CardInstance> getSelectedCardInstanceList() {
        return selectedCardInstanceList;
    }

    public void setNavigationService(NavigationService navigationService) {
        this.navigationService = navigationService;
    }

    @FXML
    public void initialize() {
        isInitializing = true;
        selectedCardInstanceList  = FXCollections.observableArrayList();
        cardRepository = CardRepository.getInstance();

        selectedCardInstanceList.addListener((ListChangeListener<CardInstance>) change -> {
            if (isInitializing) {
                return;
            }
            while (change.next()) {
                if (change.wasRemoved()) {
                    for (CardInstance removedInstance : change.getRemoved()) {
                        Card prototype = removedInstance.getCardDefinition();
                        prototype.setSelectedAmount(prototype.getSelectedAmount() - 1);
                    }
                }
                if (change.wasAdded()) {
                    for (CardInstance addedInstance : change.getAddedSubList()) {
                        Card prototype = addedInstance.getCardDefinition();
                        prototype.setSelectedAmount(prototype.getSelectedAmount() + 1);
                    }
                }
            }
            mainTabPane.getTabs().forEach(tab -> {
                Node content = tab.getContent();
                if (content != null && content.getUserData() instanceof FactionTabController) {
                    FactionTabController controller = (FactionTabController) content.getUserData();
                    controller.updateTabSelectedCardsDisplay();
                    controller.updateSelectedCountLabel();
                    controller.refreshAllCardsDisplayStyles();
                }
            });
        });

        loadCards();
        createFactionTabs();
        setupButtonActions();
        isInitializing = false;
    }

    private void loadCards() {
        List<Card> loadedPrototypes = this.cardRepository.getAllCards();
        if (loadedPrototypes == null || loadedPrototypes.isEmpty()) {
            showErrorTab("Fehler: Keine Karten zum Anzeigen geladen.");
            return;
        }
        this.allCardsMasterList = new ArrayList<>(loadedPrototypes);
        List<CardInstance> initialSelection = new ArrayList<>();
        for (Card prototype : allCardsMasterList) {
            if (!prototype.isUnlocked()) {
                prototype.setSelectedAmount(0);
            } else {
                int validSelection = Math.min(prototype.getSelectedAmount(), prototype.getMaxAmount());
                if (prototype.getSelectedAmount() != validSelection) {
                    prototype.setSelectedAmount(validSelection);
                }
                for (int i = 0; i < validSelection; i++) {
                    initialSelection.add(new CardInstance(prototype));
                }
            }
        }
        selectedCardInstanceList.addAll(initialSelection);
    }

    private void setupButtonActions() {
        if (globalSaveButton != null) {
            globalSaveButton.setOnAction(event -> handleSaveButtonAction());
        }
        if (backToMenuButton != null) {
            backToMenuButton.setOnAction(this::handleBackToMenuButtonAction);
        }
    }

    public void createFactionTabs() {
        if (mainTabPane == null) return;
        mainTabPane.getTabs().clear();
        for (Faction faction : Faction.values()) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource(FXML_FACTION_TAB_PATH));
                Parent tabContentRoot = loader.load();
                tabContentRoot.setUserData(loader.getController());
                FactionTabController factionController = loader.getController();
                factionController.initializeData(faction, this.allCardsMasterList, this.selectedCardInstanceList, this);
                Tab factionTab = new Tab(faction.name());
                factionTab.setContent(tabContentRoot);
                mainTabPane.getTabs().add(factionTab);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void handleSaveButtonAction() {
        if (this.cardRepository != null) {
            this.cardRepository.save();
            showToast("Deck successfully saved!", 2000);
        } else {
            showToast("Error: Could not save deck.", 2000);
        }
    }

    @FXML
    private void handleBackToMenuButtonAction(ActionEvent event) {
        if (navigationService != null) {
            navigationService.navigateTo(FXML_MAINMENU_PATH, "My Awesome Card Game - Main Menu", (MainMenuController controller) -> {
                controller.setNavigationService(navigationService);
            });
        }
    }

    public void showToast(String message, int duration) {
        if (mainTabPane != null && mainTabPane.getScene() != null) {
            Stage stage = (Stage) mainTabPane.getScene().getWindow();
            Toast.makeText(stage, message);
        }
    }

    private void showErrorTab(String message) {
        if (mainTabPane != null) {
            Tab errorTab = new Tab("Error");
            errorTab.setContent(new Label(message));
            mainTabPane.getTabs().add(errorTab);
        }
    }
}