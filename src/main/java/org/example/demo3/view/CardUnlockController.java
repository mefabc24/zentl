package org.example.demo3.view;

import javafx.animation.*;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import org.example.demo3.model.cards.Card;
import org.example.demo3.model.enums.DropType;
import org.example.demo3.model.enums.Rarity;
import org.example.demo3.model.logic.CardRepository;
import org.example.demo3.model.service.NavigationService;
import org.example.demo3.model.service.RarityDropService;
import javafx.application.Platform;

import java.net.URL;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import static org.example.demo3.model.constants.Config.CAROUSEL_ELEMENT_SIZE;
import static org.example.demo3.model.constants.Config.CAROUSEL_SELECTED_INDEX;
import static org.example.demo3.model.constants.Config.IMAGE_PATH;

public class CardUnlockController {

    @FXML private StackPane rootPane;

    private NavigationService navigationService;
    private final CardRepository cardRepository = CardRepository.getInstance();
    private final RarityDropService dropService = RarityDropService.getInstance();
    private final List<Card> cards = cardRepository.getUnlockableCards();

    private static final double BASE_WIDTH = 1920.0;
    private static final double BASE_HEIGHT = 1080.0;
    private static final double BASE_DROP_BUTTON_SIZE = 300.0;
    private static final double BASE_DROP_SPACING = 50.0;
    private static final double BASE_PRICE_FONT_SIZE = 20.0;
    private static final double BASE_COIN_ICON_SIZE = 24.0;
    private static final double BASE_NEW_CARD_FONT_SIZE = 48.0;
    private static final double BASE_RARITY_NODE_SIZE = 400.0;
    private static final double BASE_RARITY_SPACING = 30.0;
    private static final double BASE_CAROUSEL_HEIGHT = 450.0;
    private static final double BASE_INDICATOR_WIDTH = 5.0;
    private static final double BASE_INDICATOR_HEIGHT = 440.0;

    private final DoubleProperty scale = new SimpleDoubleProperty(1.0);
    private static final Map<Rarity, String> RARITY_IMAGE_MAP = new EnumMap<>(Rarity.class);

    static {
        RARITY_IMAGE_MAP.put(Rarity.COMMON, "CommonSlot.png");
        RARITY_IMAGE_MAP.put(Rarity.RARE, "RareSlot.png");
        RARITY_IMAGE_MAP.put(Rarity.EPIC, "EpicSlot.png");
        RARITY_IMAGE_MAP.put(Rarity.LEGENDARY, "LegendarySlot.png");
        RARITY_IMAGE_MAP.put(Rarity.MYTHIC, "MythicSlot.png");
    }

    public void setNavigationService(NavigationService navigationService) {
        this.navigationService = navigationService;
    }

    @FXML
    private void initialize() {
        rootPane.widthProperty().addListener((obs, oldVal, newVal) -> updateScale());
        rootPane.heightProperty().addListener((obs, oldVal, newVal) -> updateScale());
        showDropSelection();
    }

    private void updateScale() {
        if (rootPane.getWidth() == 0 || rootPane.getHeight() == 0) return;
        double widthScale = rootPane.getWidth() / BASE_WIDTH;
        double heightScale = rootPane.getHeight() / BASE_HEIGHT;
        scale.set(Math.min(widthScale, heightScale));
    }

    private void showDropSelection() {
        HBox dropSelectionView = new HBox();
        dropSelectionView.spacingProperty().bind(scale.multiply(BASE_DROP_SPACING));
        dropSelectionView.setAlignment(Pos.CENTER);
        dropSelectionView.getChildren().addAll(
                createDropButton(DropType.BASIC, "100"),
                createDropButton(DropType.PREMIUM, "500"),
                createDropButton(DropType.ELITE, "1000")
        );
        rootPane.getChildren().clear();
        rootPane.getChildren().add(dropSelectionView);
    }

    private void handleDropSelection(DropType dropType) {
        Card wonCard = drawActualCard(dropType);
        if (wonCard == null) {
            System.err.println("Fehler beim Ziehen der Karte. Breche ab.");
            return;
        }
        System.out.println(dropType.name() + " Drop ausgewählt. Gewonnene Karte: " + wonCard.getName() + " (" + wonCard.getRarity() + ")");

        Node dropSelectionView = rootPane.getChildren().get(0);
        FadeTransition ftOut = new FadeTransition(Duration.millis(300), dropSelectionView);
        ftOut.setToValue(0);
        ftOut.setOnFinished(e -> {
            rootPane.getChildren().clear();
            startCarouselAnimation(wonCard, dropType); // Übergibt die GEWONNENE Karte
        });
        ftOut.play();
    }


    private Card drawActualCard(DropType dropType) {
        List<Card> unlockableCards = cards.stream()
                .filter(c -> c.getAmount() < c.getRarity().getMaxAmount())
                .toList();
        if (unlockableCards.isEmpty()) {
            System.err.println("Keine freischaltbaren Karten mehr vorhanden!");
            return null;
        }

        for (int i = 0; i < 100; i++) {
            Rarity drawnRarity = dropService.getRandomRarity(dropType);
            List<Card> eligibleCards = unlockableCards.stream()
                    .filter(c -> c.getRarity() == drawnRarity)
                    .toList();
            if (!eligibleCards.isEmpty()) {
                return eligibleCards.get(ThreadLocalRandom.current().nextInt(eligibleCards.size()));
            }
        }
        System.out.println("Konnte nach 100 Versuchen keine passende Karte ziehen, nehme eine zufällige verfügbare.");
        return unlockableCards.get(ThreadLocalRandom.current().nextInt(unlockableCards.size()));
    }

    private void startCarouselAnimation(Card wonCard, DropType dropType) {
        StackPane carouselPane = new StackPane();
        carouselPane.maxHeightProperty().bind(scale.multiply(BASE_CAROUSEL_HEIGHT));

        Rectangle clipRect = new Rectangle();
        clipRect.widthProperty().bind(carouselPane.widthProperty());
        clipRect.heightProperty().bind(carouselPane.heightProperty());
        carouselPane.setClip(clipRect);

        HBox rarityContainer = new HBox();
        rarityContainer.spacingProperty().bind(scale.multiply(BASE_RARITY_SPACING));
        rarityContainer.setAlignment(Pos.CENTER_LEFT);

        Rectangle indicator = new Rectangle();
        indicator.widthProperty().bind(scale.multiply(BASE_INDICATOR_WIDTH));
        indicator.heightProperty().bind(scale.multiply(BASE_INDICATOR_HEIGHT));
        indicator.setFill(Color.web("#FFD700"));
        indicator.setStroke(Color.BLACK);
        indicator.setMouseTransparent(true);

        carouselPane.getChildren().addAll(rarityContainer, indicator);
        rootPane.getChildren().add(carouselPane);

        Rarity displayRarity = wonCard.getRarity();
        List<Integer> carouselList = createVisualRarityList(displayRarity, dropService.getProbabilityTable(dropType));
        for (int rarityId : carouselList) {
            rarityContainer.getChildren().add(createRarityNode(rarityId));
        }

        Platform.runLater(() -> {
            TranslateTransition tt = new TranslateTransition(Duration.seconds(4), rarityContainer);
            double scaledNodeWidth = scale.get() * BASE_RARITY_NODE_SIZE;
            double scaledSpacing = scale.get() * BASE_RARITY_SPACING;
            double viewPortCenter = carouselPane.getWidth() / 2.0;
            double targetNodeStartX = CAROUSEL_SELECTED_INDEX * (scaledNodeWidth + scaledSpacing);
            double targetX = -targetNodeStartX + viewPortCenter - (scaledNodeWidth / 2.0);
            tt.setToX(targetX);
            tt.setInterpolator(Interpolator.EASE_OUT);
            tt.setOnFinished(event -> startRevealSequence(wonCard, rarityContainer, indicator));
            tt.play();
        });
    }

    private void startRevealSequence(Card wonCard, HBox rarityContainer, Rectangle indicator) {
        indicator.setVisible(false);
        Node winningNode = rarityContainer.getChildren().get(CAROUSEL_SELECTED_INDEX);
        StackPane carouselPane = (StackPane) rarityContainer.getParent();

        for (Node node : rarityContainer.getChildren()) {
            if (node != winningNode) {
                FadeTransition ft = new FadeTransition(Duration.millis(300), node);
                ft.setToValue(0);
                ft.play();
            }
        }

        double scaledNodeWidth = scale.get() * BASE_RARITY_NODE_SIZE;
        double currentX = winningNode.getLayoutX() + rarityContainer.getTranslateX();
        double targetX = (carouselPane.getWidth() / 2.0) - (scaledNodeWidth / 2.0);
        double deltaX = targetX - currentX;

        TranslateTransition moveToCenter = new TranslateTransition(Duration.millis(600), winningNode);
        moveToCenter.setByX(deltaX);
        moveToCenter.setInterpolator(Interpolator.EASE_BOTH);
        ScaleTransition popEffect = new ScaleTransition(Duration.millis(600), winningNode);
        popEffect.setToX(1.1);
        popEffect.setToY(1.1);
        popEffect.setInterpolator(Interpolator.EASE_BOTH);
        ParallelTransition centerAnimation = new ParallelTransition(moveToCenter, popEffect);

        final int[] revealClicks = {0};
        centerAnimation.setOnFinished(e -> {
            ScaleTransition clickAnimation = new ScaleTransition(Duration.millis(80), winningNode);
            clickAnimation.setFromX(1.1);
            clickAnimation.setFromY(1.1);
            clickAnimation.setToX(1.0);
            clickAnimation.setToY(1.0);
            clickAnimation.setCycleCount(2);
            clickAnimation.setAutoReverse(true);
            winningNode.setOnMouseClicked(event -> {
                revealClicks[0]++;
                clickAnimation.playFromStart();
                if (revealClicks[0] >= 3) {
                    winningNode.setOnMouseClicked(null);
                    revealFinalCard(wonCard, winningNode);
                }
            });
        });
        centerAnimation.play();
    }

    private void revealFinalCard(Card wonCard, Node rarityNode) {
        ImageView cardImage = new ImageView(wonCard.getImage());
        cardImage.fitHeightProperty().bind(scale.multiply(BASE_RARITY_NODE_SIZE * 1.5));
        cardImage.setPreserveRatio(true);
        cardImage.setEffect(new DropShadow(20, Color.BLACK));
        cardImage.setOpacity(0);
        cardImage.setScaleX(0.1);
        cardImage.setScaleY(0.1);

        Label newCardLabel = new Label("NEW!");
        newCardLabel.styleProperty().bind(Bindings.concat(
                "-fx-font-size: ", scale.multiply(BASE_NEW_CARD_FONT_SIZE).asString(), "px; ",
                "-fx-font-weight: bold; -fx-text-fill: #FFD700;"
        ));
        newCardLabel.setEffect(new DropShadow(10, Color.BLACK));
        newCardLabel.setOpacity(0);
        StackPane.setAlignment(newCardLabel, Pos.TOP_CENTER);

        rootPane.getChildren().add(cardImage);
        if (wonCard.getAmount() <= 0) {
            rootPane.getChildren().add(newCardLabel);
        }

        FadeTransition ftOut = new FadeTransition(Duration.millis(400), rarityNode.getParent().getParent());
        ftOut.setToValue(0);
        FadeTransition ftInCard = new FadeTransition(Duration.millis(500), cardImage);
        ftInCard.setToValue(1);
        ScaleTransition stInCard = new ScaleTransition(Duration.millis(500), cardImage);
        stInCard.setToX(1);
        stInCard.setToY(1);
        ParallelTransition revealTransition = new ParallelTransition(ftOut, ftInCard, stInCard);

        if (wonCard.getAmount() <= 0) {
            FadeTransition ftInLabel = new FadeTransition(Duration.millis(400), newCardLabel);
            ftInLabel.setDelay(Duration.millis(300));
            ftInLabel.setToValue(1);
            revealTransition.getChildren().add(ftInLabel);
        }

        revealTransition.setOnFinished(e -> {
            wonCard.setAmount(wonCard.getAmount() + 1);
            cardImage.setCursor(Cursor.HAND);
            cardImage.setOnMouseClicked(event -> showDropSelection());
        });
        revealTransition.play();
    }

    private VBox createDropButton(DropType type, String price) {
        VBox container = new VBox();
        container.spacingProperty().bind(scale.multiply(10.0));
        container.setAlignment(Pos.CENTER);
        container.setStyle("-fx-border-color: transparent; -fx-border-width: 0;");

        ImageView dropImageView = new ImageView(new Image(getClass().getResource("/org/example/demo3/assets/drops/" + type.name() + "Drop.png").toExternalForm()));
        dropImageView.fitWidthProperty().bind(scale.multiply(BASE_DROP_BUTTON_SIZE));
        dropImageView.fitHeightProperty().bind(scale.multiply(BASE_DROP_BUTTON_SIZE));

        Button button = new Button();
        button.setGraphic(dropImageView);
        button.getStyleClass().add("drop-button");
        button.setOnAction(e -> handleDropSelection(type));

        Label priceLabel = new Label(price);
        priceLabel.styleProperty().bind(Bindings.concat(
                "-fx-font-size: ", scale.multiply(BASE_PRICE_FONT_SIZE).asString(), "px; ",
                "-fx-text-fill: white;"
        ));
        ImageView coinIcon = new ImageView(new Image(getClass().getResource("/org/example/demo3/assets/icons/Coin.png").toExternalForm()));
        coinIcon.fitHeightProperty().bind(scale.multiply(BASE_COIN_ICON_SIZE));
        coinIcon.fitWidthProperty().bind(scale.multiply(BASE_COIN_ICON_SIZE));
        HBox priceBox = new HBox(5, priceLabel, coinIcon);
        priceBox.setAlignment(Pos.CENTER);
        container.getChildren().addAll(button, priceBox);
        setupHoverAnimation(container, dropImageView);
        return container;
    }

    private void setupHoverAnimation(VBox container, ImageView imageView) {
        ScaleTransition stIn = new ScaleTransition(Duration.millis(200), imageView);
        stIn.setToX(1.05);
        stIn.setToY(1.05);
        ScaleTransition stOut = new ScaleTransition(Duration.millis(200), imageView);
        stOut.setToX(1.0);
        stOut.setToY(1.0);
        container.setOnMouseEntered(e -> {
            stIn.play();
        });
        container.setOnMouseExited(e -> {
            stOut.play();
        });
    }

    private Node createRarityNode(int rarityId) {
        Rarity rarity = Rarity.fromSortID(rarityId);
        String imageName = RARITY_IMAGE_MAP.getOrDefault(rarity, "CommonSlot.png");
        String fullPath = IMAGE_PATH + imageName;
        URL imageUrl = getClass().getResource(fullPath);
        if (imageUrl == null) {
            System.err.println("FATAL: Slot-Bildressource nicht gefunden: " + fullPath);
            return new StackPane();
        }
        ImageView imageView = new ImageView(new Image(imageUrl.toExternalForm()));
        imageView.fitWidthProperty().bind(scale.multiply(BASE_RARITY_NODE_SIZE));
        imageView.fitHeightProperty().bind(scale.multiply(BASE_RARITY_NODE_SIZE));
        imageView.setPreserveRatio(true);
        imageView.setEffect(new DropShadow(10, Color.BLACK));
        imageView.setPickOnBounds(true);
        return imageView;
    }

    private List<Integer> createVisualRarityList(Rarity selectedRarity, Map<Rarity, Double> probabilityTable) {
        List<Integer> weightedList = new ArrayList<>();
        if (probabilityTable == null) probabilityTable = dropService.getProbabilityTable(DropType.BASIC);
        probabilityTable.forEach((rarity, weight) -> {
            int count = (int) Math.round(weight * CAROUSEL_ELEMENT_SIZE);
            for (int i = 0; i < count; i++) {
                weightedList.add(rarity.getSortID());
            }
        });
        while (weightedList.size() < CAROUSEL_ELEMENT_SIZE) {
            weightedList.add(Rarity.COMMON.getSortID());
        }
        while (weightedList.size() <= CAROUSEL_SELECTED_INDEX) {
            weightedList.add(Rarity.COMMON.getSortID());
        }
        Collections.shuffle(weightedList);
        weightedList.set(CAROUSEL_SELECTED_INDEX, selectedRarity.getSortID());
        return weightedList;
    }
}