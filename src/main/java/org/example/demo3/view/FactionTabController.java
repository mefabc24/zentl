package org.example.demo3.view;

import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import org.example.demo3.model.cards.Card;
import org.example.demo3.model.cards.CardInstance;
import org.example.demo3.model.enums.Faction;
import org.example.demo3.model.logic.CardComparators;

import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

import static org.example.demo3.model.constants.Config.*;

public class FactionTabController {

    @FXML private FlowPane tabAllCardsFP;
    @FXML private FlowPane tabSelectedCardsFP;
    @FXML private ScrollPane tabCardScrollPane;
    @FXML private TextField tabSearchTextField;
    @FXML private Label tabSelectedCountLabel;

    @FXML private Button tabRaritySortButton;
    @FXML private Button tabPowerSortButton;
    @FXML private Button tabRowTypeSortButton;
    @FXML private Button tabCardTypeSortButton;
    @FXML private Button tabToggleVisibilityButton;
    @FXML private Button tabUnselectAllButton;
    @FXML private Button tabRandomizeButton;

    private Faction currentFaction;
    private List<CardInstance> factionDisplayableInstances;
    private List<CardInstance> currentlyDisplayedInstancesInTab;
    private ObservableList<CardInstance> globalSelectedCardInstancesList;

    private Comparator<CardInstance> currentSortOrderInTab = CardComparators.BY_ID_ASC_INSTANCE;
    private boolean sortAscendingInTab = true;
    private boolean showLockedCards = false;
    private InventoryController mainController;
    private Image lockedOverlayImage;

    private Node draggedItemFromSelectedCardsFP = null;
    private Button ghostNode = null;


    public void initializeData(Faction faction, List<Card> allCardPrototypes, ObservableList<CardInstance> globalSelectedInstances, InventoryController mainController) {
        this.currentFaction = faction;
        this.globalSelectedCardInstancesList = globalSelectedInstances;
        this.mainController = mainController;

        try (InputStream stream = getClass().getResourceAsStream(LOCKED_IMAGE_OVERLAY)) {
            if (stream != null) {
                this.lockedOverlayImage = new Image(stream, CARD_PREVIEW_WIDTH, CARD_PREVIEW_HEIGHT, true, true);
            }
        } catch (Exception e) {
            System.err.println("FEHLER beim Laden von LockedOverlay.png: " + e.getMessage());
        }

        this.factionDisplayableInstances = new ArrayList<>();
        allCardPrototypes.stream()
                .filter(proto -> proto.getFaction() == this.currentFaction)
                .forEach(proto -> {
                    for (int i = 0; i < proto.getAmount(); i++) {
                        this.factionDisplayableInstances.add(new CardInstance(proto));
                    }
                });
        this.currentlyDisplayedInstancesInTab = new ArrayList<>(this.factionDisplayableInstances);

        setupDragAndDropEventHandlersForPane();
        updateTabSelectedCardsDisplay();
        updateSelectedCountLabel();

        if (tabSearchTextField != null) {
            tabSearchTextField.textProperty().addListener((observable, oldValue, newValue) -> {
                filterAndDisplayCardInstancesInTab(newValue);
            });
        }

        if (tabCardScrollPane != null) {
            tabCardScrollPane.addEventFilter(ScrollEvent.SCROLL, event -> {
                if (event.getDeltaY() != 0) {
                    double contentHeight = tabCardScrollPane.getContent().getBoundsInLocal().getHeight();
                    if (contentHeight == 0) return;
                    double newVvalue = tabCardScrollPane.getVvalue() - (event.getDeltaY() * SCROLL_SPEED_MULTIPLIER / contentHeight);
                    tabCardScrollPane.setVvalue(Math.max(0, Math.min(1, newVvalue)));
                    event.consume();
                }
            });
        }
        sortAndRefreshCardInstancesInTab(this.currentSortOrderInTab);
    }

    public void updateTabSelectedCardsDisplay() {
        tabSelectedCardsFP.getChildren().clear();
        tabSelectedCardsFP.setAlignment(FLOWPANE_ALIGNMENT_SELECTED_CARDS);

        List<CardInstance> cardsForThisFactionInSelection = globalSelectedCardInstancesList.stream()
                .filter(instance -> instance.getCardDefinition().getFaction() == this.currentFaction &&
                        instance.getCardDefinition().isUnlocked())
                .collect(Collectors.toList());

        for (CardInstance instance : cardsForThisFactionInSelection) {
            Button cardButton = createSelectedCardButton(instance);
            setupDragAndDropEventHandlersForSingleCard(cardButton);
            tabSelectedCardsFP.getChildren().add(cardButton);
        }
    }

    private Button createSelectedCardButton(CardInstance instance) {
        Card cardDef = instance.getCardDefinition();
        ImageView imageView = new ImageView(cardDef.getImage());
        imageView.setFitWidth(SELECTED_CARD_PREVIEW_WIDTH);
        imageView.setFitHeight(SELECTED_CARD_PREVIEW_HEIGHT);
        imageView.setPreserveRatio(true);

        Button cardButton = new Button();
        cardButton.setGraphic(imageView);
        cardButton.setPadding(Insets.EMPTY);
        cardButton.getStyleClass().clear();
        cardButton.getStyleClass().add("selected-card-preview-button");
        cardButton.setUserData(instance);

        cardButton.setOnAction(e -> {
            if (draggedItemFromSelectedCardsFP != null && draggedItemFromSelectedCardsFP.getUserData() == instance) return;
            handleCardSelectionToggleInTab(instance, null);
        });
        Tooltip.install(cardButton, new Tooltip(cardDef.getName() + " (Entfernen)"));
        return cardButton;
    }

    private void createAndShowGhostNode() {
        if (draggedItemFromSelectedCardsFP instanceof Button && draggedItemFromSelectedCardsFP.getUserData() instanceof CardInstance) {
            removeGhostNodeFromPane();
            CardInstance instance = (CardInstance) draggedItemFromSelectedCardsFP.getUserData();
            ghostNode = createSelectedCardButton(instance);
            ghostNode.setOpacity(0.5);
            ghostNode.setMouseTransparent(true);
            ghostNode.getStyleClass().add("ghost-card");
        }
    }

    private void removeGhostNodeFromPane() {
        if (ghostNode != null) {
            if (tabSelectedCardsFP.getChildren().contains(ghostNode)) {
                tabSelectedCardsFP.getChildren().remove(ghostNode);
            }
            ghostNode = null;
        }
    }

    private void updateGhostNodePosition(double sceneX) {
        if (ghostNode == null || draggedItemFromSelectedCardsFP == null) return;

        ObservableList<Node> items = tabSelectedCardsFP.getChildren();

        if (items.contains(ghostNode)) {
            items.remove(ghostNode);
        }

        int insertIndex = 0;
        boolean positionFound = false;

        for (int i = 0; i < items.size(); i++) {
            Node child = items.get(i);

            Point2D childCenterInScene = child.localToScene(child.getBoundsInLocal().getWidth() / 2, 0);

            if (sceneX <= childCenterInScene.getX()) {
                insertIndex = i;
                positionFound = true;
                break;
            }
            insertIndex = i + 1;
        }

        if (!positionFound && !items.isEmpty() && items.get(items.size()-1) == draggedItemFromSelectedCardsFP && items.size()==1){
            Point2D childCenterInScene = draggedItemFromSelectedCardsFP.localToScene(draggedItemFromSelectedCardsFP.getBoundsInLocal().getWidth() / 2, 0);
            if(sceneX <= childCenterInScene.getX()){
                insertIndex = 0;
            } else {
                insertIndex = 1;
            }
        } else if (!positionFound && items.stream().allMatch(n -> n == draggedItemFromSelectedCardsFP)) {
            insertIndex = 0;
        }


        if (insertIndex > items.size()) {
            insertIndex = items.size();
        }

        items.add(insertIndex, ghostNode);
    }


    private void setupDragAndDropEventHandlersForPane() {
        tabSelectedCardsFP.setOnDragOver(event -> {
            if (draggedItemFromSelectedCardsFP != null && event.getGestureSource() == draggedItemFromSelectedCardsFP) {
                event.acceptTransferModes(TransferMode.MOVE);

                if (event.getPickResult().getIntersectedNode() == tabSelectedCardsFP) {
                    updateGhostNodePosition(event.getSceneX());
                }
                event.consume();
            }
        });

        tabSelectedCardsFP.setOnDragDropped(event -> {
            if (draggedItemFromSelectedCardsFP != null && ghostNode != null) {
                int ghostIndex = tabSelectedCardsFP.getChildren().indexOf(ghostNode);
                if (ghostIndex != -1) {
                    tabSelectedCardsFP.getChildren().remove(draggedItemFromSelectedCardsFP);
                    tabSelectedCardsFP.getChildren().add(ghostIndex, draggedItemFromSelectedCardsFP);
                    event.setDropCompleted(true);
                }
            }
            cleanupDragOperation();
            event.consume();
        });
    }

    private void setupDragAndDropEventHandlersForSingleCard(Node cardNode) {
        cardNode.setOnDragDetected(event -> {
            if (!(cardNode.getUserData() instanceof CardInstance) || !tabSelectedCardsFP.getChildren().contains(cardNode)) {
                return;
            }
            draggedItemFromSelectedCardsFP = cardNode;
            createAndShowGhostNode();

            Dragboard db = cardNode.startDragAndDrop(TransferMode.MOVE);
            SnapshotParameters params = new SnapshotParameters();
            params.setFill(Color.TRANSPARENT);
            Image snapshotImage = cardNode.snapshot(params, null);
            db.setDragView(snapshotImage, event.getX(), event.getY());

            ClipboardContent content = new ClipboardContent();
            content.putString("selectedCardIsBeingDragged");
            db.setContent(content);

            draggedItemFromSelectedCardsFP.setVisible(false);
            draggedItemFromSelectedCardsFP.setManaged(false);

            event.consume();
        });

        cardNode.setOnDragOver(event -> {
            if (draggedItemFromSelectedCardsFP != null &&
                    draggedItemFromSelectedCardsFP != cardNode &&
                    event.getGestureSource() == draggedItemFromSelectedCardsFP &&
                    ghostNode != null) {

                event.acceptTransferModes(TransferMode.MOVE);
                updateGhostNodePosition(event.getSceneX());
                event.consume();
            }
        });

        cardNode.setOnDragDropped(event -> {
            if (draggedItemFromSelectedCardsFP != null && ghostNode != null) {
                int ghostIndex = tabSelectedCardsFP.getChildren().indexOf(ghostNode);
                if (ghostIndex != -1) {
                    tabSelectedCardsFP.getChildren().remove(draggedItemFromSelectedCardsFP);
                    tabSelectedCardsFP.getChildren().add(ghostIndex, draggedItemFromSelectedCardsFP);
                    event.setDropCompleted(true);
                }
            }
            cleanupDragOperation();
            event.consume();
        });

        cardNode.setOnDragDone(event -> {
            cleanupDragOperation();
            event.consume();
        });
    }

    private void cleanupDragOperation() {
        removeGhostNodeFromPane();
        if (draggedItemFromSelectedCardsFP != null) {
            draggedItemFromSelectedCardsFP.setVisible(true);
            draggedItemFromSelectedCardsFP.setManaged(true);
        }
        draggedItemFromSelectedCardsFP = null;
    }

    public void refreshAllCardsDisplayStyles() {
        if (tabAllCardsFP == null) return;
        for (Node node : tabAllCardsFP.getChildren()) {
            if (node instanceof Button) {
                Button button = (Button) node;
                Object userData = button.getUserData();
                if (userData instanceof CardInstance) {
                    CardInstance instance = (CardInstance) userData;
                    updateButtonSelectionStyleInTab(button, globalSelectedCardInstancesList.contains(instance));
                }
            }
        }
    }

    public void updateSelectedCountLabel() {
        if (tabSelectedCountLabel != null) {
            long currentFactionSelectedCount = globalSelectedCardInstancesList.stream()
                    .filter(instance -> instance.getCardDefinition().getFaction() == this.currentFaction &&
                            instance.getCardDefinition().isUnlocked())
                    .count();
            tabSelectedCountLabel.setText(currentFactionSelectedCount + "/" + MAX_SELECTION);
        }
    }
    private Button findButtonForCardInstanceInTab(CardInstance instanceToFind) {
        for (Node node : tabAllCardsFP.getChildren()) {
            if (node instanceof Button && node.getUserData() == instanceToFind) {
                return (Button) node;
            }
        }
        return null;
    }

    private void handleCardSelectionToggleInTab(CardInstance instanceToToggle, Button cardInstanceButtonInTab) {
        if (instanceToToggle == null) return;
        if (draggedItemFromSelectedCardsFP != null && draggedItemFromSelectedCardsFP.getUserData() == instanceToToggle) {
            return;
        }

        Card cardDef = instanceToToggle.getCardDefinition();

        if (!cardDef.isUnlocked()) {
            if (mainController != null) mainController.showToast("Karte '" + cardDef.getName() + "' ist gesperrt!", 2000);
            return;
        }

        boolean wasSelected = globalSelectedCardInstancesList.contains(instanceToToggle);

        if (wasSelected) {
            globalSelectedCardInstancesList.remove(instanceToToggle);
        } else {
            long selectedCountForThisFaction = globalSelectedCardInstancesList.stream()
                    .filter(inst -> inst.getCardDefinition().getFaction() == this.currentFaction && inst.getCardDefinition().isUnlocked())
                    .count();

            if (selectedCountForThisFaction >= MAX_SELECTION) {
                if (mainController != null) mainController.showToast("Limit für Fraktion " + this.currentFaction.name() + " erreicht: " + MAX_SELECTION + " Karten", 2000);
                return;
            }

            long countOfThisCardTypeSelected = globalSelectedCardInstancesList.stream()
                    .filter(inst -> inst.getCardDefinition().getId() == cardDef.getId() && inst.getCardDefinition().isUnlocked())
                    .count();

            if (countOfThisCardTypeSelected >= cardDef.getMaxAmount()) {
                if (mainController != null) mainController.showToast("Max. " + cardDef.getMaxAmount() + "x '" + cardDef.getName() + "' erlaubt.", 2000);
                return;
            }

            globalSelectedCardInstancesList.add(instanceToToggle);
        }

        if (cardInstanceButtonInTab != null) {
            updateButtonSelectionStyleInTab(cardInstanceButtonInTab, !wasSelected);
        } else {
            Button associatedMainButton = findButtonForCardInstanceInTab(instanceToToggle);
            if (associatedMainButton != null) {
                updateButtonSelectionStyleInTab(associatedMainButton, !wasSelected);
            }
        }
    }

    private void filterAndDisplayCardInstancesInTab(String searchText) {
        List<CardInstance> filteredInstances;
        if (searchText == null || searchText.trim().isEmpty()) {
            filteredInstances = new ArrayList<>(this.factionDisplayableInstances);
        } else {
            String lowerCaseSearchText = searchText.toLowerCase().trim();
            filteredInstances = this.factionDisplayableInstances.stream()
                    .filter(instance -> instance.getCardDefinition().getName().toLowerCase().contains(lowerCaseSearchText))
                    .collect(Collectors.toList());
        }
        this.currentlyDisplayedInstancesInTab = filteredInstances;
        sortAndRefreshCardInstancesInTab(this.currentSortOrderInTab);
    }

    private void sortAndRefreshCardInstancesInTab(Comparator<CardInstance> comparator) {
        if (this.currentlyDisplayedInstancesInTab == null) return;

        if (this.currentSortOrderInTab != null && this.currentSortOrderInTab.equals(comparator) && comparator != CardComparators.BY_ID_ASC_INSTANCE) {
            this.sortAscendingInTab = !this.sortAscendingInTab;
        } else {
            this.sortAscendingInTab = true;
        }

        if (comparator == null && this.currentSortOrderInTab != null) {
            comparator = this.currentSortOrderInTab;
        } else if (comparator == null) {
            comparator = CardComparators.BY_RARITY_ASC_INSTANCE;
        }
        this.currentSortOrderInTab = comparator;

        Comparator<CardInstance> finalComparator = this.sortAscendingInTab ? comparator : comparator.reversed();
        finalComparator = finalComparator.thenComparing(CardComparators.BY_ID_ASC_INSTANCE);

        this.currentlyDisplayedInstancesInTab.sort(finalComparator);
        displayCardInstancesInTab(this.currentlyDisplayedInstancesInTab);
    }
    @FXML private void handleTabSortByRarity() { sortAndRefreshCardInstancesInTab(CardComparators.BY_RARITY_ASC_INSTANCE); }
    @FXML private void handleTabSortByPower() { sortAndRefreshCardInstancesInTab(CardComparators.BY_POWER_ASC_INSTANCE); }
    @FXML private void handleTabSortByRowType() { sortAndRefreshCardInstancesInTab(CardComparators.BY_ROWTYPE_ASC_INSTANCE); }
    @FXML private void handleTabSortByCardType() { sortAndRefreshCardInstancesInTab(CardComparators.BY_CARDTYPE_ASC_INSTANCE); }

    @FXML private void handleTabToggleVisibility() {
        this.showLockedCards = !this.showLockedCards;
        if (tabToggleVisibilityButton != null) tabToggleVisibilityButton.setText(this.showLockedCards ? "HIDE LOCKED" : "SHOW LOCKED");
        displayCardInstancesInTab(this.currentlyDisplayedInstancesInTab);
    }

    @FXML
    private void handleTabUnselectAll() {
        if (draggedItemFromSelectedCardsFP != null) return;
        List<CardInstance> instancesToUnselectInThisFaction = globalSelectedCardInstancesList.stream()
                .filter(instance -> instance.getCardDefinition().getFaction() == this.currentFaction && instance.getCardDefinition().isUnlocked())
                .collect(Collectors.toList());
        globalSelectedCardInstancesList.removeAll(instancesToUnselectInThisFaction);
    }

    @FXML
    private void handleTabRandomize() {
        if (draggedItemFromSelectedCardsFP != null) return;
        handleTabUnselectAll();

        List<CardInstance> potentialRandomCandidates = this.factionDisplayableInstances.stream()
                .filter(instance -> instance.getCardDefinition().isUnlocked())
                .collect(Collectors.toList());

        if (potentialRandomCandidates.isEmpty()) {
            if (mainController != null) mainController.showToast("Keine freigeschalteten Karten in dieser Fraktion zum Randomisieren verfügbar.", 2500);
            return;
        }

        Collections.shuffle(potentialRandomCandidates, new Random());
        List<CardInstance> cardsToAddTemporarily = new ArrayList<>();
        int cardsSuccessfullySelected = 0;

        for (CardInstance instanceToSelect : potentialRandomCandidates) {
            long currentFactionSelectedCount = globalSelectedCardInstancesList.stream()
                    .filter(selectedInst -> selectedInst.getCardDefinition().getFaction() == this.currentFaction)
                    .count() + cardsToAddTemporarily.stream().filter(ci -> ci.getCardDefinition().getFaction() == this.currentFaction).count();
            if (currentFactionSelectedCount >= MAX_SELECTION) break;

            long countOfThisCardTypeSelected = globalSelectedCardInstancesList.stream()
                    .filter(inst -> inst.getCardDefinition().getId() == instanceToSelect.getCardDefinition().getId())
                    .count() + cardsToAddTemporarily.stream().filter(ci -> ci.getCardDefinition().getId() == instanceToSelect.getCardDefinition().getId()).count();
            if (countOfThisCardTypeSelected >= instanceToSelect.getCardDefinition().getMaxAmount()) continue;

            cardsToAddTemporarily.add(instanceToSelect);
            cardsSuccessfullySelected++;
            if (cardsSuccessfullySelected >= RANDOMIZER_CARD_AMOUNT) break;
        }
        for(CardInstance instance : cardsToAddTemporarily){
            handleCardSelectionToggleInTab(instance, findButtonForCardInstanceInTab(instance));
        }
    }

    private void displayCardInstancesInTab(List<CardInstance> instancesToDisplay) {
        tabAllCardsFP.getChildren().clear();
        tabAllCardsFP.setPadding(new Insets(10));
        tabAllCardsFP.setHgap(10);
        tabAllCardsFP.setVgap(10);
        tabAllCardsFP.setAlignment(FLOWPANE_ALIGNMENT_ALL_CARDS);

        for (CardInstance instance : instancesToDisplay) {
            if (!showLockedCards && !instance.getCardDefinition().isUnlocked()) continue;
            Card cardDef = instance.getCardDefinition();
            Button cardButton = new Button();
            cardButton.setUserData(instance);
            StackPane cardVisualPane = new StackPane();
            cardVisualPane.setAlignment(Pos.CENTER);
            cardVisualPane.setPrefSize(CARD_PREVIEW_WIDTH, CARD_PREVIEW_HEIGHT);
            Image image = cardDef.getImage();
            Node cardDisplayNode;

            if (image != null && !image.isError()) {
                ImageView cardImageView = new ImageView(image);
                cardImageView.setFitWidth(CARD_PREVIEW_WIDTH);
                cardImageView.setFitHeight(CARD_PREVIEW_HEIGHT);
                cardImageView.setPreserveRatio(false);
                cardDisplayNode = cardImageView;
            } else {
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

            if (!cardDef.isUnlocked()) {
                if (this.lockedOverlayImage != null && !this.lockedOverlayImage.isError()) {
                    ImageView overlayImageView = new ImageView(this.lockedOverlayImage);
                    overlayImageView.setFitWidth(CARD_PREVIEW_WIDTH);
                    overlayImageView.setFitHeight(CARD_PREVIEW_HEIGHT);
                    cardVisualPane.getChildren().add(overlayImageView);
                } else {
                    Rectangle lockFallbackOverlay = new Rectangle(CARD_PREVIEW_WIDTH, CARD_PREVIEW_HEIGHT, Color.rgb(0, 0, 0, 0.5));
                    Label lockedLabel = new Label("LOCKED");
                    lockedLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 18px;");
                    StackPane.setAlignment(lockedLabel, Pos.CENTER);
                    cardVisualPane.getChildren().addAll(lockFallbackOverlay, lockedLabel);
                }
            }
            cardButton.setGraphic(cardVisualPane);
            cardButton.setPadding(Insets.EMPTY);
            cardButton.getStyleClass().clear();
            cardButton.getStyleClass().add("card-button");
            setupCardButtonInTab(cardButton, instance);
            tabAllCardsFP.getChildren().add(cardButton);
        }
    }

    private void setupCardButtonInTab(Button cardButton, CardInstance instance) {
        Card cardDef = instance.getCardDefinition();
        updateButtonSelectionStyleInTab(cardButton, globalSelectedCardInstancesList.contains(instance));
        cardButton.setOnAction(event -> handleCardSelectionToggleInTab(instance, cardButton));
        Tooltip tooltip = new Tooltip(
                "Name: " + cardDef.getName() + "\n" +
                        "Beschreibung: " + cardDef.getDescription() + "\n" +
                        "Stärke: " + cardDef.getPower() + "\n" +
                        "Fraktion: " + cardDef.getFaction() + "\n" +
                        "Typ: " + cardDef.getRowType() + "\n" +
                        "CardType: " + cardDef.getCardType() + "\n" +
                        "Seltenheit: " + cardDef.getRarity() + "\n" +
                        "Status: " + (cardDef.isUnlocked() ? "Freigeschaltet" : "Gesperrt"));
        tooltip.setFont(new Font("System", 20));
        cardButton.setTooltip(tooltip);
    }

    private void updateButtonSelectionStyleInTab(Button button, boolean isThisInstanceSelected) {
        Object userData = button.getUserData();
        if (!(userData instanceof CardInstance)) return;
        CardInstance instance = (CardInstance) userData;
        Card cardDef = instance.getCardDefinition();

        if (button.getParent() == tabAllCardsFP) {
            button.getStyleClass().remove("selected");
            if (isThisInstanceSelected && cardDef.isUnlocked()) {
                button.getStyleClass().add("selected");
            }
        }
        button.getStyleClass().remove("locked-card-visual");
        if (!cardDef.isUnlocked()) {
            button.getStyleClass().add("locked-card-visual");
        }
    }
}