package org.example.demo3.view;

import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.*;
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

    // FXML Fields
    @FXML private FlowPane tabAllCardsFP;
    @FXML private FlowPane tabSelectedCardsFP;
    @FXML private ScrollPane tabCardScrollPane;
    @FXML private TextField tabSearchTextField;
    @FXML private Label tabSelectedCountLabel;
    @FXML private Button tabToggleVisibilityButton;

    // Member Variables
    private Faction currentFaction;
    private List<CardInstance> factionDisplayableInstances;
    private List<CardInstance> currentlyDisplayedInstancesInTab;
    private ObservableList<CardInstance> globalSelectedCardInstancesList;
    private final Map<CardInstance, Button> instanceToButtonMap = new HashMap<>();
    private InventoryController mainController;
    private Image lockedOverlayImage;

    // State Variables
    private Comparator<CardInstance> currentSortOrderInTab = CardComparators.BY_ID_ASC_INSTANCE;
    private boolean sortAscendingInTab = true;
    private boolean showLockedCards = false;

    // Drag and Drop State (Original)
    private Node draggedItemFromSelectedCardsFP = null;
    private Button ghostNode = null;


    public void initializeData(Faction faction, List<CardInstance> factionInstances, ObservableList<CardInstance> globalSelectedInstances, InventoryController mainController) {
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

        this.factionDisplayableInstances = new ArrayList<>(factionInstances);
        this.currentlyDisplayedInstancesInTab = new ArrayList<>(this.factionDisplayableInstances);

        setupListeners();
        setupDragAndDropEventHandlersForPane();
        updateTabSelectedCardsDisplay();
        updateSelectedCountLabel();
        sortAndRefreshCardInstancesInTab(this.currentSortOrderInTab);
    }

    private void setupListeners() {
        this.globalSelectedCardInstancesList.addListener((ListChangeListener<CardInstance>) change -> {
            if (draggedItemFromSelectedCardsFP == null) {
                updateTabSelectedCardsDisplay();
            }
            updateSelectedCountLabel();
            refreshAllCardsDisplayStyles();
        });

        if (tabSearchTextField != null) {
            tabSearchTextField.textProperty().addListener((obs, oldVal, newVal) -> filterAndDisplayCardInstancesInTab(newVal));
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
    }

    private void displayCardInstancesInTab(List<CardInstance> instancesToDisplay) {
        tabAllCardsFP.getChildren().clear();
        tabAllCardsFP.setPadding(new Insets(10));
        tabAllCardsFP.setHgap(10);
        tabAllCardsFP.setVgap(10);
        tabAllCardsFP.setAlignment(FLOWPANE_ALIGNMENT_ALL_CARDS);
        instanceToButtonMap.clear();

        List<Button> cardButtons = instancesToDisplay.stream()
                .filter(instance -> showLockedCards || instance.getCardDefinition().getAmount() > 0)
                .map(this::createDisplayCardButton)
                .toList();

        tabAllCardsFP.getChildren().addAll(cardButtons);
    }

    public void updateTabSelectedCardsDisplay() {
        tabSelectedCardsFP.getChildren().clear();
        tabSelectedCardsFP.setAlignment(FLOWPANE_ALIGNMENT_SELECTED_CARDS);

        List<Button> selectedButtons = globalSelectedCardInstancesList.stream()
                .filter(instance -> instance.getCardDefinition().getFaction() == this.currentFaction)
                .map(this::createSelectedCardButton)
                .toList();

        tabSelectedCardsFP.getChildren().addAll(selectedButtons);
    }

    private Button createDisplayCardButton(CardInstance instance) {
        Button cardButton = new Button();
        cardButton.setUserData(instance);
        cardButton.setGraphic(createCardVisual(instance.getCardDefinition(), CARD_PREVIEW_WIDTH, CARD_PREVIEW_HEIGHT));
        cardButton.setPadding(Insets.EMPTY);
        cardButton.getStyleClass().add("card-button");

        cardButton.setOnAction(event -> handleCardSelectionToggleInTab(instance));

        updateButtonSelectionStyleInTab(cardButton, globalSelectedCardInstancesList.contains(instance));
        Tooltip.install(cardButton, createCardTooltip(instance.getCardDefinition()));

        instanceToButtonMap.put(instance, cardButton);
        return cardButton;
    }

    private Button createSelectedCardButton(CardInstance instance) {
        Card cardDef = instance.getCardDefinition();
        Button cardButton = new Button();

        cardButton.setGraphic(createCardVisual(cardDef, SELECTED_CARD_PREVIEW_WIDTH, SELECTED_CARD_PREVIEW_HEIGHT));
        cardButton.setPadding(Insets.EMPTY);
        cardButton.getStyleClass().clear();
        cardButton.getStyleClass().add("selected-card-preview-button");
        cardButton.setUserData(instance);

        cardButton.setOnAction(e -> {
            if (draggedItemFromSelectedCardsFP != null && draggedItemFromSelectedCardsFP.getUserData() == instance) return;
            handleCardSelectionToggleInTab(instance);
        });

        setupDragAndDropEventHandlersForSingleCard(cardButton);
        Tooltip.install(cardButton, new Tooltip(cardDef.getName() + " (Entfernen)"));
        return cardButton;
    }

    private Node createCardVisual(Card cardDef, double width, double height) {
        StackPane cardVisualPane = new StackPane(createCardImageOrPlaceholder(cardDef, width, height));
        cardVisualPane.setAlignment(Pos.CENTER);
        cardVisualPane.setPrefSize(width, height);

        if (cardDef.getAmount() <= 0) {
            if (this.lockedOverlayImage != null && !this.lockedOverlayImage.isError()) {
                ImageView overlayImageView = new ImageView(this.lockedOverlayImage);
                overlayImageView.setFitWidth(width);
                overlayImageView.setFitHeight(height);
                cardVisualPane.getChildren().add(overlayImageView);
            }
        }
        return cardVisualPane;
    }

    private Node createCardImageOrPlaceholder(Card cardDef, double width, double height) {
        Image image = cardDef.getImage();
        if (image != null && !image.isError()) {
            ImageView cardImageView = new ImageView(image);
            cardImageView.setFitWidth(width);
            cardImageView.setFitHeight(height);
            return cardImageView;
        } else {
            Label noImageLabel = new Label("Bild\nfehlt");
            noImageLabel.setWrapText(true);
            noImageLabel.setStyle("-fx-text-alignment: center; -fx-text-fill: black; -fx-font-weight: bold;");
            Rectangle placeholderRect = new Rectangle(width, height, Color.LIGHTGREY);
            placeholderRect.setStroke(Color.DARKGREY);
            return new StackPane(placeholderRect, noImageLabel);
        }
    }

    private Tooltip createCardTooltip(Card cardDef) {
        String tooltipText = String.format(
                "Name: %s\nBeschreibung: %s\nStärke: %d\nFraktion: %s\nTyp: %s\nCardType: %s\nSeltenheit: %s\nStatus: %s",
                cardDef.getName(), cardDef.getDescription(), cardDef.getPower(), cardDef.getFaction(),
                cardDef.getRowType(), cardDef.getCardType(), cardDef.getRarity(),
                (cardDef.getAmount() > 0 ? "Freigeschaltet" : "Gesperrt")
        );
        Tooltip tooltip = new Tooltip(tooltipText);
        tooltip.setFont(new Font("System", 20));
        return tooltip;
    }

    private void createAndShowGhostNode() {
        if (draggedItemFromSelectedCardsFP instanceof Button && draggedItemFromSelectedCardsFP.getUserData() instanceof CardInstance instance) {
            removeGhostNodeFromPane();
            ghostNode = createSelectedCardButton(instance);
            ghostNode.setOpacity(0.5);
            ghostNode.setMouseTransparent(true);
            ghostNode.getStyleClass().add("ghost-card");
        }
    }

    private void removeGhostNodeFromPane() {
        if (ghostNode != null) {
            tabSelectedCardsFP.getChildren().remove(ghostNode);
            ghostNode = null;
        }
    }

    private void updateGhostNodePosition(double sceneX) {
        if (ghostNode == null || draggedItemFromSelectedCardsFP == null) return;
        ObservableList<Node> items = tabSelectedCardsFP.getChildren();
        items.remove(ghostNode);

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
        if (!positionFound && !items.isEmpty() && items.getLast() == draggedItemFromSelectedCardsFP && items.size()==1){
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

        tabSelectedCardsFP.setOnDragDropped(this::handleDragDropped);
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

        cardNode.setOnDragDropped(this::handleDragDropped);
        cardNode.setOnDragDone(event -> {
            cleanupDragOperation();
            event.consume();
        });
    }

    private void handleDragDropped(DragEvent event) {
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
    }

    private void cleanupDragOperation() {
        removeGhostNodeFromPane();
        if (draggedItemFromSelectedCardsFP != null) {
            draggedItemFromSelectedCardsFP.setVisible(true);
            draggedItemFromSelectedCardsFP.setManaged(true);
        }
        draggedItemFromSelectedCardsFP = null;
    }

    private void handleCardSelectionToggleInTab(CardInstance instanceToToggle) {
        if (instanceToToggle == null || (draggedItemFromSelectedCardsFP != null && draggedItemFromSelectedCardsFP.getUserData() == instanceToToggle)) {
            return;
        }
        if (globalSelectedCardInstancesList.contains(instanceToToggle)) {
            globalSelectedCardInstancesList.remove(instanceToToggle);
        } else {
            if (canSelectCard(instanceToToggle)) {
                globalSelectedCardInstancesList.add(instanceToToggle);
            }
        }
    }

    private boolean canSelectCard(CardInstance instance) {
        Card cardDef = instance.getCardDefinition();
        if (cardDef.getAmount() <= 0) {
            mainController.showToast("Karte '" + cardDef.getName() + "' ist gesperrt!", 2000);
            return false;
        }

        long factionCount = globalSelectedCardInstancesList.stream()
                .filter(inst -> inst.getCardDefinition().getFaction() == this.currentFaction).count();
        if (factionCount >= MAX_SELECTION) {
            mainController.showToast("Limit für Fraktion " + this.currentFaction.name() + " erreicht.", 2000);
            return false;
        }

        long sameCardCount = globalSelectedCardInstancesList.stream()
                .filter(inst -> inst.getCardDefinition().getId() == cardDef.getId()).count();
        if (sameCardCount >= cardDef.getMaxAmount()) {
            mainController.showToast("Max. " + cardDef.getMaxAmount() + "x '" + cardDef.getName() + "' erlaubt.", 2000);
            return false;
        }
        return true;
    }

    public void refreshAllCardsDisplayStyles() {
        instanceToButtonMap.forEach((instance, button) ->
                updateButtonSelectionStyleInTab(button, globalSelectedCardInstancesList.contains(instance))
        );
    }

    private void updateButtonSelectionStyleInTab(Button button, boolean isSelected) {
        CardInstance instance = (CardInstance) button.getUserData();
        Card cardDef = instance.getCardDefinition();

        button.getStyleClass().removeAll("selected", "locked-card-visual");

        if (isSelected && cardDef.getAmount() > 0) {
            button.getStyleClass().add("selected");
        }
        if (cardDef.getAmount() <= 0) {
            button.getStyleClass().add("locked-card-visual");
        }
    }

    public void updateSelectedCountLabel() {
        if (tabSelectedCountLabel != null) {
            long count = globalSelectedCardInstancesList.stream()
                    .filter(instance -> instance.getCardDefinition().getFaction() == this.currentFaction).count();
            tabSelectedCountLabel.setText(count + "/" + MAX_SELECTION);
        }
    }

    private void filterAndDisplayCardInstancesInTab(String searchText) {
        String lowerCaseSearchText = (searchText == null) ? "" : searchText.toLowerCase().trim();
        this.currentlyDisplayedInstancesInTab = this.factionDisplayableInstances.stream()
                .filter(instance -> lowerCaseSearchText.isEmpty() || instance.getCardDefinition().getName().toLowerCase().contains(lowerCaseSearchText))
                .collect(Collectors.toList());
        sortAndRefreshCardInstancesInTab(this.currentSortOrderInTab);
    }

    private void sortAndRefreshCardInstancesInTab(Comparator<CardInstance> comparator) {
        if (this.currentSortOrderInTab != null && this.currentSortOrderInTab.equals(comparator) && comparator != CardComparators.BY_ID_ASC_INSTANCE) {
            this.sortAscendingInTab = !this.sortAscendingInTab;
        } else {
            this.sortAscendingInTab = true;
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
        tabToggleVisibilityButton.setText(this.showLockedCards ? "HIDE LOCKED" : "SHOW LOCKED");
        displayCardInstancesInTab(this.currentlyDisplayedInstancesInTab);
    }

    @FXML private void handleTabUnselectAll() {
        if (draggedItemFromSelectedCardsFP != null) return;
        List<CardInstance> toRemove = globalSelectedCardInstancesList.stream()
                .filter(instance -> instance.getCardDefinition().getFaction() == this.currentFaction)
                .toList();
        globalSelectedCardInstancesList.removeAll(toRemove);
    }

    @FXML private void handleTabRandomize() {
        if (draggedItemFromSelectedCardsFP != null) return;
        handleTabUnselectAll();

        List<CardInstance> candidates = this.factionDisplayableInstances.stream()
                .filter(instance -> instance.getCardDefinition().getAmount() > 0)
                .collect(Collectors.toList());
        Collections.shuffle(candidates);

        List<CardInstance> toAdd = new ArrayList<>();
        for (CardInstance candidate : candidates) {
            if (globalSelectedCardInstancesList.size() + toAdd.size() >= MAX_SELECTION) break;

            List<CardInstance> tempList = new ArrayList<>(globalSelectedCardInstancesList);
            tempList.addAll(toAdd);

            if (canSelectCardWithList(candidate, tempList)) {
                toAdd.add(candidate);
            }
        }
        globalSelectedCardInstancesList.addAll(toAdd);
    }

    private boolean canSelectCardWithList(CardInstance instance, List<CardInstance> list) {
        Card cardDef = instance.getCardDefinition();
        long factionCount = list.stream().filter(inst -> inst.getCardDefinition().getFaction() == this.currentFaction).count();
        if (factionCount >= MAX_SELECTION) return false;

        long sameCardCount = list.stream().filter(inst -> inst.getCardDefinition().getId() == cardDef.getId()).count();
        return sameCardCount < cardDef.getMaxAmount();
    }
}