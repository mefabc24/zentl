package org.example.demo3.view;

import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import org.example.demo3.model.cards.Card;
import org.example.demo3.model.enums.Faction;
import org.example.demo3.model.logic.CardComparators;

import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

import static org.example.demo3.model.constants.InventoryConstants.*;

public class FactionTabController {

    @FXML private FlowPane tabAllCardsFP;
    @FXML private FlowPane tabSelectedCardsFP;
    @FXML private ScrollPane tabCardScrollPane;
    @FXML private TextField tabSearchTextField;
    @FXML private Label tabSelectedCountLabel;

    // UI-Elemente für Aktionen im Tab
    @FXML private Button tabRaritySortButton;
    @FXML private Button tabPowerSortButton;
    @FXML private Button tabRowTypeSortButton;
    @FXML private Button tabCardTypeSortButton;
    @FXML private Button tabUnselectAllButton;
    @FXML private Button tabRandomizeButton;

    // Interne Zustandsvariablen
    private Faction currentFaction;
    private List<Card> allCardsMasterList;
    private List<Card> factionSpecificMasterList;
    private List<Card> currentlyDisplayedCardsInTab;
    private ObservableList<Card> globalSelectedCardsList;

    private Comparator<Card> currentSortOrderInTab = CardComparators.BY_RARITY_ASC;
    private boolean sortAscendingInTab = true;

    private InventoryController mainController;

    private Image lockedOverlayImage;

    public void initializeData(Faction faction, List<Card> allCardsMaster, ObservableList<Card> globalSelectedCards, InventoryController mainController) {
        this.currentFaction = faction;
        this.allCardsMasterList = allCardsMaster;
        this.globalSelectedCardsList = globalSelectedCards;
        this.mainController = mainController;

        // Lade das Overlay-Bild
        try (InputStream stream = getClass().getResourceAsStream(LOCKED_IMAGE_OVERLAY)) {
            if (stream != null) {
                this.lockedOverlayImage = new Image(stream, CARD_PREVIEW_WIDTH, CARD_PREVIEW_HEIGHT, true, true); // Lädt mit Zielgröße
            } else {
                System.err.println("FEHLER: LockedOverlay.png nicht gefunden unter Pfad: " + LOCKED_IMAGE_OVERLAY);
            }
        } catch (Exception e) {
            System.err.println("FEHLER beim Laden von LockedOverlay.png: " + e.getMessage());
            e.printStackTrace();
        }

        // Filtere Karten, die zu dieser Fraktion gehören
        this.factionSpecificMasterList = this.allCardsMasterList.stream()
                .filter(card -> card.getFaction() == this.currentFaction)
                .collect(Collectors.toList());

        this.currentlyDisplayedCardsInTab = new ArrayList<>(this.factionSpecificMasterList);

        // Listener für Änderungen an der globalen Liste der ausgewählten Karten
        this.globalSelectedCardsList.addListener((ListChangeListener<Card>) change -> {
            updateTabSelectedCardsDisplay();
            updateSelectedCountLabel();
        });

        // Initiale UI-Updates
        updateTabSelectedCardsDisplay();
        updateSelectedCountLabel();

        // Listener für das Suchfeld
        if (tabSearchTextField != null) {
            tabSearchTextField.textProperty().addListener((observable, oldValue, newValue) -> {
                filterAndDisplayCardsInTab(newValue);
            });
        }

        // Scroll-Geschwindigkeit für das Karten-ScrollPane anpassen
        if (tabCardScrollPane != null) {
            tabCardScrollPane.addEventFilter(ScrollEvent.SCROLL, event -> {
                if (event.getDeltaY() != 0) {
                    double contentHeight = tabCardScrollPane.getContent().getBoundsInLocal().getHeight();
                    if (contentHeight == 0) return; // Verhindere Division durch Null
                    double newVvalue = tabCardScrollPane.getVvalue() - (event.getDeltaY() * SCROLL_SPEED_MULTIPLIER / contentHeight);
                    tabCardScrollPane.setVvalue(Math.max(0, Math.min(1, newVvalue))); // Begrenze Vvalue zwischen 0 und 1
                    event.consume();
                }
            });
        }
        // Initiales Sortieren und Anzeigen der Karten

        sortAndRefreshCardsInTab(this.currentSortOrderInTab);
    }

    private void updateSelectedCountLabel() {
        if (tabSelectedCountLabel != null) {
            long currentFactionSelectedCount = globalSelectedCardsList.stream()
                    .filter(card -> card.getFaction() == this.currentFaction && card.isUnlocked()) // Zähle nur freigeschaltete ausgewählte Karten
                    .count();
            tabSelectedCountLabel.setText(currentFactionSelectedCount + "/" + MAX_SELECTION);
        }
    }

    private void updateTabSelectedCardsDisplay() {
        tabSelectedCardsFP.getChildren().clear();
        tabSelectedCardsFP.setAlignment(FLOWPANE_ALIGNMENT_SELECTED_CARDS); // Konstante für Ausrichtung

        for (Card card : globalSelectedCardsList) {
            // Zeige nur Karten an, die zur aktuellen Fraktion des Tabs gehören und freigeschaltet sind
            if (card.getFaction() == this.currentFaction && card.isUnlocked()) {
                ImageView imageView = new ImageView(card.getImage());
                imageView.setFitWidth(SELECTED_CARD_PREVIEW_WIDTH);
                imageView.setFitHeight(SELECTED_CARD_PREVIEW_HEIGHT);
                imageView.setPreserveRatio(true);

                Button removeButton = new Button();
                removeButton.setGraphic(imageView);
                removeButton.setPadding(Insets.EMPTY);
                removeButton.getStyleClass().clear();
                removeButton.getStyleClass().add("selected-card-preview-button");

                removeButton.setOnAction(e -> {
                    handleCardSelectionToggleInTab(card, findButtonForCardInTab(card));
                });
                javafx.scene.control.Tooltip.install(removeButton, new javafx.scene.control.Tooltip(card.getName() + " (Entfernen)"));
                tabSelectedCardsFP.getChildren().add(removeButton);
            }
        }
    }

    private Button findButtonForCardInTab(Card card) {
        for (Node node : tabAllCardsFP.getChildren()) {
            if (node instanceof Button && node.getUserData() == card) {
                return (Button) node;
            }
        }
        return null;
    }

    private void handleCardSelectionToggleInTab(Card card, Button mainCardButtonInTab) {
        if (card == null) return;

        // Interaktion mit gesperrten Karten verhindern
        if (!card.isUnlocked()) {
            System.out.println("Karte '" + card.getName() + "' ist gesperrt und kann nicht ausgewählt werden.");
            if (mainController != null) {
                mainController.showTemporaryMessage("Karte '" + card.getName() + "' ist gesperrt!", 2000);
            }
            return; // Auswahlaktion abbrechen
        }

        long selectedCountForThisFaction = globalSelectedCardsList.stream()
                .filter(selectedCard -> selectedCard.getFaction() == this.currentFaction && selectedCard.isUnlocked())
                .count();

        // Prüfen, ob die Karte neu ausgewählt werden soll UND das Limit für DIESE Fraktion erreicht ist.
        if (!card.isSelected() && selectedCountForThisFaction >= MAX_SELECTION) {
            System.out.println("Maximale Auswahl von " + MAX_SELECTION + " Karten für Fraktion '" +
                    this.currentFaction.name() + "' erreicht. Karte '" + card.getName() + "' nicht hinzugefügt.");
            if (mainController != null) {
                mainController.showTemporaryMessage("Limit für Fraktion " + this.currentFaction.name() +
                        " erreicht: " + MAX_SELECTION + " Karten", 2000);
            }
            return; // Auswahlaktion abbrechen
        }

        card.toggleSelected(); // Ändere den Auswahlstatus der Karte

        // Aktualisiere die globale Liste der ausgewählten Karten
        if (card.isSelected()) {
            if (!globalSelectedCardsList.contains(card)) {
                globalSelectedCardsList.add(card);
            }
        } else {
            globalSelectedCardsList.remove(card);
        }

        // Aktualisiere das Styling des Buttons im Haupt-FlowPane
        if (mainCardButtonInTab != null) {
            updateButtonSelectionStyleInTab(mainCardButtonInTab, card.isSelected());
        } else {
            // Falls von der "Selected Cards"-Ansicht getriggert, finde den zugehörigen Button
            Button associatedMainButton = findButtonForCardInTab(card);
            if (associatedMainButton != null) {
                updateButtonSelectionStyleInTab(associatedMainButton, card.isSelected());
            }
        }
        // Das Zähler-Label wird durch den Listener auf globalSelectedCardsList aktualisiert.
    }

    private void filterAndDisplayCardsInTab(String searchText) {
        List<Card> filteredCards;
        if (searchText == null || searchText.trim().isEmpty()) {
            filteredCards = new ArrayList<>(this.factionSpecificMasterList);
        } else {
            String lowerCaseSearchText = searchText.toLowerCase().trim();
            filteredCards = this.factionSpecificMasterList.stream()
                    .filter(card -> card.getName().toLowerCase().contains(lowerCaseSearchText))
                    .collect(Collectors.toList());
        }
        this.currentlyDisplayedCardsInTab = filteredCards;
        sortAndRefreshCardsInTab(this.currentSortOrderInTab); // Neu sortieren und anzeigen
    }

    private void sortAndRefreshCardsInTab(Comparator<Card> comparator) {
        if (this.currentlyDisplayedCardsInTab == null) return;

        // Umschalten der Sortierrichtung, wenn derselbe Comparator erneut verwendet wird
        if (this.currentSortOrderInTab != null && this.currentSortOrderInTab.equals(comparator) && comparator != CardComparators.BY_ID_ASC) {
            this.sortAscendingInTab = !this.sortAscendingInTab;
        } else {
            this.sortAscendingInTab = true; // Standardmäßig aufsteigend für neuen Comparator
        }

        // Setze den aktuellen Comparator (oder Fallback)
        if (comparator == null && this.currentSortOrderInTab != null) {
            comparator = this.currentSortOrderInTab;
        } else if (comparator == null) {
            comparator = CardComparators.BY_RARITY_ASC; // Fallback-Sortierung
        }
        this.currentSortOrderInTab = comparator;

        // Erstelle den finalen Comparator basierend auf der Sortierrichtung
        Comparator<Card> finalComparator = this.sortAscendingInTab ? comparator : comparator.reversed();
        finalComparator = finalComparator.thenComparing(CardComparators.BY_ID_ASC); // Stabile Sortierung nach ID

        Collections.sort(this.currentlyDisplayedCardsInTab, finalComparator);
        displayCardsInTab(this.currentlyDisplayedCardsInTab); // Aktualisiere die Kartenanzeige
    }

    // --- FXML Action Handler ---
    @FXML
    private void handleTabSortByRarity() {
        sortAndRefreshCardsInTab(CardComparators.BY_RARITY_ASC);
    }

    @FXML
    private void handleTabSortByPower() {
        sortAndRefreshCardsInTab(CardComparators.BY_POWER_ASC);
    }

    @FXML
    private void handleTabSortByRowType() {
        sortAndRefreshCardsInTab(CardComparators.BY_ROWTYPE_ASC);
    }

    @FXML
    private void handleTabSortByCardType() {
        sortAndRefreshCardsInTab(CardComparators.BY_CARDTYPE_ASC);
    }

    @FXML
    private void handleTabUnselectAll() {
        // Sammle Karten dieser Fraktion, die aktuell ausgewählt und freigeschaltet sind
        List<Card> cardsToUnselectInThisFaction = globalSelectedCardsList.stream()
                .filter(card -> card.getFaction() == this.currentFaction && card.isUnlocked())
                .collect(Collectors.toList());

        // Aktualisiere den Status und das UI für jede dieser Karten
        for (Card card : cardsToUnselectInThisFaction) {
            card.setSelected(false);
            Button mainButton = findButtonForCardInTab(card);
            if (mainButton != null) {
                updateButtonSelectionStyleInTab(mainButton, false);
            }
        }
        // Entferne die Karten in einem Schwung aus der globalen Liste (löst Listener aus)
        globalSelectedCardsList.removeAll(cardsToUnselectInThisFaction);
    }

    @FXML
    private void handleTabRandomize() {
        handleTabUnselectAll(); // Deselektiert fraktionsspezifisch, aktualisiert Zähler

        if (this.factionSpecificMasterList != null && !this.factionSpecificMasterList.isEmpty()) {
            // Nur freigeschaltete Karten für Randomize berücksichtigen
            List<Card> unlockedCardsInFaction = this.factionSpecificMasterList.stream()
                    .filter(Card::isUnlocked)
                    .collect(Collectors.toList());

            if (unlockedCardsInFaction.isEmpty()) {
                if (mainController != null) {
                    mainController.showTemporaryMessage("Keine freigeschalteten Karten in dieser Fraktion zum Randomisieren verfügbar.", 2500);
                }
                return;
            }

            Collections.shuffle(unlockedCardsInFaction, new Random());

            int cardsSuccessfullySelected = 0;
            for (Card card : unlockedCardsInFaction) {
                long currentFactionSelectedCount = globalSelectedCardsList.stream()
                        .filter(selectedCard -> selectedCard.getFaction() == this.currentFaction && selectedCard.isUnlocked())
                        .count();

                if (currentFactionSelectedCount < MAX_SELECTION && cardsSuccessfullySelected < RANDOMIZER_CARD_AMOUNT) {
                    if (!card.isSelected()) {
                        int previousGlobalSelectedCount = globalSelectedCardsList.size();
                        handleCardSelectionToggleInTab(card, findButtonForCardInTab(card));
                        if (globalSelectedCardsList.size() > previousGlobalSelectedCount && globalSelectedCardsList.contains(card)) {
                            cardsSuccessfullySelected++;
                        }
                    }
                } else if (currentFactionSelectedCount >= MAX_SELECTION) {
                    System.out.println("Randomizer: Fraktionslimit für " + this.currentFaction.name() + " erreicht.");
                    break;
                }
                if (cardsSuccessfullySelected >= RANDOMIZER_CARD_AMOUNT) {
                    System.out.println("Randomizer: RANDOMIZER_CARD_AMOUNT ("+RANDOMIZER_CARD_AMOUNT+") für " + this.currentFaction.name() + " erreicht.");
                    break;
                }
            }
        }
    }

    /**
     * Zeichnet die Karten-Buttons im Haupt-FlowPane.
     * @param cardsToDisplay Die Liste der anzuzeigenden Karten.
     */
    private void displayCardsInTab(List<Card> cardsToDisplay) {
        tabAllCardsFP.getChildren().clear();
        tabAllCardsFP.setPadding(new Insets(10));
        tabAllCardsFP.setHgap(10);
        tabAllCardsFP.setVgap(10);
        tabAllCardsFP.setAlignment(FLOWPANE_ALIGNMENT_ALL_CARDS);

        for (Card card : cardsToDisplay) {
            Button cardButton = new Button();
            cardButton.setUserData(card); // Wichtig für spätere Identifizierung

            // StackPane für das Kartenbild und ggf. das Overlay
            StackPane cardVisualPane = new StackPane();
            cardVisualPane.setAlignment(Pos.CENTER); // Sicherstellen, dass Kinder zentriert sind
            // Setze die bevorzugte Größe des StackPanes, damit der Button die richtige Größe hat
            cardVisualPane.setPrefSize(CARD_PREVIEW_WIDTH, CARD_PREVIEW_HEIGHT);


            // Kartenbild
            Image image = card.getImage(); // getImage() sollte das Bild laden
            Node cardDisplayNode; // Wird entweder ImageView oder Platzhalter

            if (image != null && !image.isError()) {
                ImageView cardImageView = new ImageView(image);
                cardImageView.setFitWidth(CARD_PREVIEW_WIDTH);
                cardImageView.setFitHeight(CARD_PREVIEW_HEIGHT);
                cardImageView.setPreserveRatio(false); // Beibehaltung des Seitenverhältnisses ist meistens gut
                cardDisplayNode = cardImageView;
            } else {
                if (image != null && image.getException() != null) {
                    System.err.println("Fehler beim Laden des Bildes für Karte " + card.getName() + ": " + image.getException().getMessage());
                }
                Label noImageLabel = new Label("Bild\nfehlt");
                noImageLabel.setWrapText(true);
                noImageLabel.setStyle("-fx-text-alignment: center; -fx-text-fill: black; -fx-font-weight: bold;");
                Rectangle placeholderRect = new Rectangle(CARD_PREVIEW_WIDTH, CARD_PREVIEW_HEIGHT);
                placeholderRect.setFill(Color.LIGHTGREY);
                placeholderRect.setStroke(Color.DARKGREY);
                cardDisplayNode = new StackPane(placeholderRect, noImageLabel);
                StackPane.setAlignment(noImageLabel, Pos.CENTER);
            }
            cardVisualPane.getChildren().add(cardDisplayNode);

            // Overlay für gesperrte Karten
            if (!card.isUnlocked() && this.lockedOverlayImage != null && !this.lockedOverlayImage.isError()) {
                ImageView overlayImageView = new ImageView(this.lockedOverlayImage);

                cardVisualPane.getChildren().add(overlayImageView);
            } else if (!card.isUnlocked() && (this.lockedOverlayImage == null || this.lockedOverlayImage.isError())) {
                // Fallback, falls das Overlay-Bild nicht geladen werden konnte
                Rectangle lockFallbackOverlay = new Rectangle(CARD_PREVIEW_WIDTH, CARD_PREVIEW_HEIGHT, Color.rgb(0,0,0,0.5)); // Halbtransparent schwarz
                Label lockedLabel = new Label("LOCKED");
                lockedLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 18px;");
                StackPane.setAlignment(lockedLabel, Pos.CENTER);
                cardVisualPane.getChildren().addAll(lockFallbackOverlay, lockedLabel);
            }

            cardButton.setGraphic(cardVisualPane);
            cardButton.setPadding(Insets.EMPTY);

            cardButton.getStyleClass().clear();
            cardButton.getStyleClass().add("card-button");

            setupCardButtonInTab(cardButton, card);
            tabAllCardsFP.getChildren().add(cardButton);
        }
    }

    private void setupCardButtonInTab(Button cardButton, Card card) {
        // Aktualisiert den Selektionsstil basierend auf card.isSelected() und card.isUnlocked()
        updateButtonSelectionStyleInTab(cardButton, card.isSelected());
        // Setzt die Aktion für den Klick auf den Button
        cardButton.setOnAction(event -> handleCardSelectionToggleInTab(card, cardButton));

        // Tooltip für den Button erstellen
        javafx.scene.control.Tooltip tooltip = new javafx.scene.control.Tooltip(
                "Name: " + card.getName() + "\n" +
                        "Beschreibung: " + card.getDescription() + "\n" +
                        "Stärke: " + card.getPower() + "\n" +
                        "Fraktion: " + card.getFaction() + "\n" +
                        "Typ: " + card.getRowType() + "\n" +
                        "CardType: " + card.getCardType() + "\n" +
                        "Seltenheit: " + card.getRarity() + "\n" +
                        "Status: " + (card.isUnlocked() ? "Freigeschaltet" : "Gesperrt")
        );
        tooltip.setFont(new Font("System", 20));
        cardButton.setTooltip(tooltip);
    }

    private void updateButtonSelectionStyleInTab(Button button, boolean isSelected) {
        Card card = (Card) button.getUserData(); // Hole die Karte, um den Unlocked-Status zu prüfen
        if (card == null) return; // Sicherheitsprüfung

        if (button.getParent() == tabAllCardsFP) {
            if (!button.getStyleClass().contains("card-button")) {
                button.getStyleClass().clear();
                button.getStyleClass().add("card-button");
            }
        } else if (button.getParent() == tabSelectedCardsFP) {
            if (!button.getStyleClass().contains("selected-card-preview-button")) {
                button.getStyleClass().clear();
                button.getStyleClass().add("selected-card-preview-button");
            }
            return;
        }

        if (isSelected && card.isUnlocked()) {
            if (!button.getStyleClass().contains("selected")) {
                button.getStyleClass().add("selected");
            }
        } else {
            button.getStyleClass().remove("selected");
        }

        if (!card.isUnlocked()) {
            if (!button.getStyleClass().contains("locked-card-visual")) {
                button.getStyleClass().add("locked-card-visual");
            }
        } else {
            button.getStyleClass().remove("locked-card-visual");
        }
    }
}