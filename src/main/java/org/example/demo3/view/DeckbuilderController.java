package org.example.demo3.view;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import org.example.demo3.model.cards.Card;
import org.example.demo3.model.cards.CardInstance;
import org.example.demo3.model.cards.UnitCard;
import org.example.demo3.model.effect.Toast;
import org.example.demo3.model.enums.*;
import org.example.demo3.model.logic.CardRepository;
import org.example.demo3.model.service.NavigationService;

import java.io.InputStream;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.example.demo3.model.constants.Config.*;

public class DeckbuilderController {

    //<editor-fold desc="FXML Fields">
    // Faction Navigation
    @FXML private Button prevFactionButton;
    @FXML private Label prevFactionLabel;
    @FXML private Label activeFactionLabel;
    @FXML private Button nextFactionButton;
    @FXML private Label nextFactionLabel;

    // Left Pane (Card Collection)
    @FXML private Label leftFilterLabel;
    @FXML private Button leftFilterAllButton;
    @FXML private Button leftFilterMeleeButton;
    @FXML private Button leftFilterRangedButton;
    @FXML private Button leftFilterSiegeButton;
    @FXML private Button leftFilterAnyButton;
    @FXML private Button leftFilterWeatherButton;
    @FXML private Button leftFilterSpecialButton;
    @FXML private ScrollPane leftScrollPane;
    @FXML private FlowPane cardCollectionFlowPane;

    // Middle Pane (Stats & Actions)
    @FXML private Label totalCardsInDeckLabel;
    @FXML private Label selectedCardsLabel;
    @FXML private Label totalUnitStrengthLabel;
    @FXML private Label selectedUnitStrengthLabel;
    @FXML private Button unlockButton;
    @FXML private Button menuButton;
    @FXML private Button saveButton;

    // Right Pane (Selected Cards)
    @FXML private Label rightFilterLabel;
    @FXML private Button rightFilterAllButton;
    @FXML private Button rightFilterMeleeButton;
    @FXML private Button rightFilterRangedButton;
    @FXML private Button rightFilterSiegeButton;
    @FXML private Button rightFilterAnyButton;
    @FXML private Button rightFilterWeatherButton;
    @FXML private Button rightFilterSpecialButton;
    @FXML private ScrollPane rightScrollPane;
    @FXML private FlowPane cardSelectionFlowPane;
    //</editor-fold>

    private NavigationService navigationService;
    private CardRepository cardRepository;

    private List<Faction> factions;
    private int currentFactionIndex;

    private List<CardInstance> allOwnedCardInstances;
    private ObservableList<CardInstance> selectedInstances;

    // Filter-Management
    private Predicate<CardInstance> leftFilter = instance -> true;
    private Predicate<CardInstance> rightFilter = instance -> true;
    private final Map<Button, Predicate<CardInstance>> filterPredicates = new HashMap<>();
    private final Map<Button, String> filterLabels = new HashMap<>();
    private Button activeLeftFilterButton;
    private Button activeRightFilterButton;

    // NEU: Listen zur expliziten Gruppierung der Buttons
    private List<Button> leftFilterButtons;
    private List<Button> rightFilterButtons;


    public void setNavigationService(NavigationService navigationService) {
        this.navigationService = navigationService;
    }

    @FXML
    public void initialize() {
        this.cardRepository = CardRepository.getInstance();
        this.selectedInstances = FXCollections.observableArrayList();



        loadInitialDeckState();
        setupFactionSlider();

        // NEU: Buttons explizit gruppieren
        groupFilterButtons();

        setupFilterButtons();
        setupActionButtons();
        setupSelectionListener();

        updateFactionView();
    }

    private void loadInitialDeckState() {
        List<Card> allCards = cardRepository.getAllCards();
        if (allCards == null || allCards.isEmpty()) {
            activeFactionLabel.setText("ERROR: NO CARDS");
            return;
        }

        this.allOwnedCardInstances = allCards.stream()
                .filter(card -> card.getAmount() > 0)
                .flatMap(card -> Collections.nCopies(card.getAmount(), new CardInstance(card)).stream())
                .collect(Collectors.toList());

        List<CardInstance> initialSelection = new ArrayList<>();
        for (Card card : allCards) {
            if (card.getSelectedAmount() > 0) {
                allOwnedCardInstances.stream()
                        .filter(inst -> inst.getCardDefinition().getId() == card.getId())
                        .filter(inst -> !initialSelection.contains(inst)) // Sicherstellen, dass wir nicht dieselbe Instanz mehrmals hinzufügen
                        .limit(card.getSelectedAmount())
                        .forEach(initialSelection::add);
            }
        }
        selectedInstances.addAll(initialSelection);
    }

    private void setupFactionSlider() {
        this.factions = Arrays.asList(Faction.values());
        this.currentFactionIndex = 0;

        prevFactionButton.setOnAction(e -> navigateFaction(-1));
        nextFactionButton.setOnAction(e -> navigateFaction(1));
    }

    // NEU: Hilfsmethode zur Gruppierung der Buttons
    private void groupFilterButtons() {
        leftFilterButtons = Arrays.asList(
                leftFilterAllButton, leftFilterMeleeButton, leftFilterRangedButton,
                leftFilterSiegeButton, leftFilterAnyButton, leftFilterWeatherButton, leftFilterSpecialButton
        );
        rightFilterButtons = Arrays.asList(
                rightFilterAllButton, rightFilterMeleeButton, rightFilterRangedButton,
                rightFilterSiegeButton, rightFilterAnyButton, rightFilterWeatherButton, rightFilterSpecialButton
        );
    }

    /**
     * Konfiguriert alle Filter-Buttons für beide Seiten.
     * KORRIGIERTE VERSION
     */
    private void setupFilterButtons() {
        // Filter-Definitionen (bleibt gleich)
        addFilter(leftFilterAllButton, rightFilterAllButton, "ALL CARDS", i -> true);
        addFilter(leftFilterMeleeButton, rightFilterMeleeButton, "MELEE CARDS", i -> i.getCardDefinition().getRowType() == RowType.MELEE);
        addFilter(leftFilterRangedButton, rightFilterRangedButton, "RANGED CARDS", i -> i.getCardDefinition().getRowType() == RowType.RANGED);
        addFilter(leftFilterSiegeButton, rightFilterSiegeButton, "SIEGE CARDS", i -> i.getCardDefinition().getRowType() == RowType.SIEGE);
        addFilter(leftFilterAnyButton, rightFilterAnyButton, "ANY ROW CARDS", i -> i.getCardDefinition().getRowType() == RowType.ANY);
        addFilter(leftFilterWeatherButton, rightFilterWeatherButton, "WEATHER CARDS", i -> i.getCardDefinition().getCardType() == CardType.WEATHER);
        addFilter(leftFilterSpecialButton, rightFilterSpecialButton, "SPECIAL CARDS", i -> i.getCardDefinition().getCardType() == CardType.SPECIAL);

        // Event Handler für die linke Seite, jetzt mit der expliziten Liste
        for (Button button : leftFilterButtons) {
            button.setOnAction(e -> {
                leftFilter = filterPredicates.get(button);
                leftFilterLabel.setText(filterLabels.get(button));
                setActiveFilterButton(button, true);
                populateCollectionPane();
            });
        }

        // Event Handler für die rechte Seite, jetzt mit der expliziten Liste
        for (Button button : rightFilterButtons) {
            button.setOnAction(e -> {
                rightFilter = filterPredicates.get(button);
                rightFilterLabel.setText(filterLabels.get(button));
                setActiveFilterButton(button, false);
                populateSelectionPane();
            });
        }

        // Initialen aktiven Button setzen (bleibt gleich)
        setActiveFilterButton(leftFilterAllButton, true);
        setActiveFilterButton(rightFilterAllButton, false);
    }

    private void addFilter(Button leftButton, Button rightButton, String label, Predicate<CardInstance> predicate) {
        filterPredicates.put(leftButton, predicate);
        filterPredicates.put(rightButton, predicate);
        filterLabels.put(leftButton, label);
        filterLabels.put(rightButton, label);
    }

    private void setActiveFilterButton(Button button, boolean isLeft) {
        if (isLeft) {
            if (activeLeftFilterButton != null) activeLeftFilterButton.setStyle("-fx-opacity: 0.5");
            activeLeftFilterButton = button;
            if (activeLeftFilterButton != null) activeLeftFilterButton.setStyle("-fx-opacity: 1.0");
        } else {
            if (activeRightFilterButton != null) activeRightFilterButton.setStyle("-fx-opacity: 0.5");
            activeRightFilterButton = button;
            if (activeRightFilterButton != null) activeRightFilterButton.setStyle("-fx-opacity: 1.0");
        }
    }


    private void setupActionButtons() {
        saveButton.setOnAction(e -> {
            cardRepository.getAllCards().forEach(card -> card.setSelectedAmount(0));
            selectedInstances.stream()
                    .map(CardInstance::getCardDefinition)
                    .forEach(card -> card.setSelectedAmount(card.getSelectedAmount() + 1));

            cardRepository.save();
            Toast.makeText((getStage()), "Deck successfully saved!", 2000);
        });

        menuButton.setOnAction(e -> {
            System.out.println("Menu Button clicked");
            if (navigationService != null) {
                navigationService.navigateTo(FXML_MAINMENU_PATH, "Gwent", (MainMenuController controller) ->
                        controller.setNavigationService(navigationService));
            } else {
                System.err.println("NavigationService not initialized in MainMenuController.");
            }
        });

        unlockButton.setOnAction(e -> {
            System.out.println("Unlock Button clicked");
            if (navigationService != null) {
                navigationService.navigateTo(FXML_UNLOCKER_PATH, "Gwent", (CardUnlockController controller) ->
                        controller.setNavigationService(navigationService));
            } else {
                System.err.println("NavigationService not initialized in MainMenuController.");
            }
        });
    }

    private void setupSelectionListener() {
        selectedInstances.addListener((ListChangeListener<CardInstance>) change -> {
            populateCollectionPane(); // Wichtig: Auch linke Seite neu zeichnen, um die Anzahl anzuzeigen
            populateSelectionPane();
            updateStats();
        });
    }

    private void navigateFaction(int direction) {
        currentFactionIndex = (currentFactionIndex + direction + factions.size()) % factions.size();
        updateFactionView();
    }

    private void updateFactionView() {
        Faction current = factions.get(currentFactionIndex);
        Faction prev = factions.get((currentFactionIndex - 1 + factions.size()) % factions.size());
        Faction next = factions.get((currentFactionIndex + 1) % factions.size());

        activeFactionLabel.setText(current.name());
        prevFactionLabel.setText(prev.name());
        nextFactionLabel.setText(next.name());

        leftFilter = p -> true;
        rightFilter = p -> true;
        leftFilterLabel.setText("ALL CARDS");
        rightFilterLabel.setText("ALL CARDS");
        setActiveFilterButton(leftFilterAllButton, true);
        setActiveFilterButton(rightFilterAllButton, false);


        populateCollectionPane();
        populateSelectionPane();
        updateStats();
    }

    private void populateCollectionPane() {
        cardCollectionFlowPane.getChildren().clear();
        Faction currentFaction = factions.get(currentFactionIndex);

        Map<Integer, Card> uniqueCards = allOwnedCardInstances.stream()
                .map(CardInstance::getCardDefinition)
                .filter(card -> card.getFaction() == currentFaction)
                .collect(Collectors.toMap(Card::getId, card -> card, (existing, replacement) -> existing));

        uniqueCards.values().stream()
                .filter(card -> leftFilter.test(new CardInstance(card)))
                .sorted(Comparator.comparing(Card::getName))
                .forEach(card -> {
                    Node cardNode = createCardVisual(card, this::addCardToDeck, true);
                    cardCollectionFlowPane.getChildren().add(cardNode);
                });
    }

    private void populateSelectionPane() {
        cardSelectionFlowPane.getChildren().clear();
        Faction currentFaction = factions.get(currentFactionIndex);

        selectedInstances.stream()
                .filter(instance -> instance.getCardDefinition().getFaction() == currentFaction)
                .filter(rightFilter)
                .sorted(Comparator.comparing(i -> i.getCardDefinition().getName()))
                .forEach(instance -> {
                    Node cardNode = createCardVisual(instance.getCardDefinition(), this::removeCardFromDeck, false);
                    cardSelectionFlowPane.getChildren().add(cardNode);
                });
    }

    private void updateStats() {
        Faction currentFaction = factions.get(currentFactionIndex);

        long currentFactionSelectedCount = selectedInstances.stream()
                .filter(i -> i.getCardDefinition().getFaction() == currentFaction)
                .count();

        long totalOwnedForFaction = allOwnedCardInstances.stream()
                .filter(i -> i.getCardDefinition().getFaction() == currentFaction)
                .count();

        int totalStrength = allOwnedCardInstances.stream()
                .filter(i -> i.getCardDefinition().getFaction() == currentFaction)
                .filter(i -> i.getCardDefinition() instanceof UnitCard)
                .mapToInt(i -> i.getCardDefinition().getPower())
                .sum();

        int selectedStrength = selectedInstances.stream()
                .filter(i -> i.getCardDefinition().getFaction() == currentFaction)
                .filter(i -> i.getCardDefinition() instanceof UnitCard)
                .mapToInt(i -> i.getCardDefinition().getPower())
                .sum();

        totalCardsInDeckLabel.setText(String.valueOf(totalOwnedForFaction));
        selectedCardsLabel.setText(String.valueOf(currentFactionSelectedCount));
        selectedUnitStrengthLabel.setText(String.valueOf(selectedStrength));
        totalUnitStrengthLabel.setText(String.valueOf(totalStrength));
    }


    private void addCardToDeck(Card card) {
        Faction currentFaction = factions.get(currentFactionIndex);

        long totalInDeckForFaction = selectedInstances.stream()
                .filter(i -> i.getCardDefinition().getFaction() == currentFaction)
                .count();

        if (totalInDeckForFaction >= MAX_SELECTION) {
            Toast.makeText(getStage(), "Deck for " + currentFaction.name() + " is full.", 2000);
            return;
        }

        long countOfThisCardInDeck = selectedInstances.stream()
                .filter(i -> i.getCardDefinition().getId() == card.getId())
                .count();

        if (countOfThisCardInDeck >= card.getMaxAmount()) {
            Toast.makeText(getStage(), "Maximum amount of " + card.getName() + " reached.", 2000);
            return;
        }

        allOwnedCardInstances.stream()
                .filter(inst -> inst.getCardDefinition().getId() == card.getId())
                .filter(inst -> !selectedInstances.contains(inst))
                .findFirst()
                .ifPresent(selectedInstances::add);
    }

    private void removeCardFromDeck(Card card) {
        selectedInstances.stream()
                .filter(i -> i.getCardDefinition().getId() == card.getId())
                .reduce((first, second) -> second)
                .ifPresent(selectedInstances::remove);
    }

    private Node createCardVisual(Card card, java.util.function.Consumer<Card> action, boolean showCount) {
        StackPane visualRoot = new StackPane();

        visualRoot.setPrefSize(145, 220);
        visualRoot.setMinSize(145, 220);
        visualRoot.setMaxSize(145, 220);

        ImageView cardImageView = new ImageView();
        Image image = card.getImage();
        if (image != null && !image.isError()) {
            cardImageView.setImage(image);
        } else {
            Rectangle placeholder = new Rectangle(145, 220, Color.DARKSLATEGRAY);
            Label nameLabel = new Label(card.getName());
            nameLabel.setWrapText(true);
            nameLabel.setTextFill(Color.WHITE);
            visualRoot.getChildren().addAll(placeholder, nameLabel);
        }
        cardImageView.setFitWidth(145);
        cardImageView.setFitHeight(220);
        if(image != null && !image.isError()) visualRoot.getChildren().add(cardImageView);

        if (showCount) {
            long countInDeck = selectedInstances.stream().filter(i -> i.getCardDefinition().getId() == card.getId()).count();
            long maxAmount = card.getAmount();

            Label countLabel = new Label(countInDeck + "/" + maxAmount);
            countLabel.setStyle("-fx-background-color: rgba(0, 0, 0, 0.7); -fx-text-fill: white; -fx-padding: 2 5; -fx-background-radius: 10;");
            StackPane.setAlignment(countLabel, Pos.BOTTOM_RIGHT);
            StackPane.setMargin(countLabel, new Insets(0, 5, 5, 0));
            visualRoot.getChildren().add(countLabel);
        }


        Button cardButton = new Button();
        cardButton.setGraphic(visualRoot);
        cardButton.setPadding(Insets.EMPTY);
        cardButton.getStyleClass().add("card-button");
        cardButton.setOnAction(e -> action.accept(card));

        Tooltip.install(cardButton, createCardTooltip(card));

        return cardButton;
    }


    private Tooltip createCardTooltip(Card cardDef) {
        String tooltipText = String.format(
                "Name: %s\nStärke: %d\nReihe: %s\nTyp: %s\nSeltenheit: %s",
                cardDef.getName(), cardDef.getPower(), cardDef.getRowType(),
                cardDef.getCardType(), cardDef.getRarity()
        );
        Tooltip tooltip = new Tooltip(tooltipText);
        tooltip.setStyle("-fx-font-size: 14px;");
        return tooltip;
    }

    private javafx.stage.Stage getStage() {
        if (activeFactionLabel != null && activeFactionLabel.getScene() != null) {
            return (javafx.stage.Stage) activeFactionLabel.getScene().getWindow();
        }
        return null;
    }
}