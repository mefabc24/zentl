package org.example.demo3.view;

import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import org.example.demo3.model.cards.Card;
import org.example.demo3.model.enums.Rarity;
import org.example.demo3.model.logic.CardRepository;
import org.example.demo3.model.service.NavigationService;

import java.net.URL;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.example.demo3.model.constants.Config.*;

public class CardUnlockController {
    @FXML private StackPane carouselPane;
    @FXML private HBox rarityContainer;
    @FXML private Button spinButton;

    private NavigationService navigationService;
    private final CardRepository cardRepository = CardRepository.getInstance();
    private final List<Card> cards = cardRepository.getUnlockableCards();
    private static final double NODE_WIDTH = 300.0;
    private static final double NODE_HEIGHT = 300.0;
    private static final double SPACING = 30.0;
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
        Rectangle clipRect = new Rectangle();
        clipRect.widthProperty().bind(carouselPane.widthProperty());
        clipRect.heightProperty().bind(carouselPane.heightProperty());
        carouselPane.setClip(clipRect);
    }

    @FXML
    public void generateCard() {
        if (spinButton != null) {
            spinButton.setDisable(true);
        }

        Card card = getRandomCard();
        if (card == null) {
            System.err.println("Keine freischaltbare Karte gefunden. Breche ab.");
            if (spinButton != null) spinButton.setDisable(false);
            return;
        }

        rarityContainer.getChildren().clear();
        rarityContainer.setTranslateX(0);
        List<Integer> carousselList = getRarityVisualList(card.getRarity());
        for (int rarityId : carousselList) {
            rarityContainer.getChildren().add(createRarityNode(rarityId));
        }

        Platform.runLater(() -> {
            double viewPortCenter = carouselPane.getWidth() / 2.0;
            double targetNodeStartX = CAROUSEL_SELECTED_INDEX * (NODE_WIDTH + SPACING);
            double targetX = -targetNodeStartX + viewPortCenter - (NODE_WIDTH / 2.0);

            System.out.println("--- Animations-Berechnung (in Platform.runLater) ---");
            System.out.println("carouselPane.getWidth(): " + carouselPane.getWidth());
            System.out.println("Viewport Center: " + viewPortCenter);
            System.out.println("Ziel-Index: " + CAROUSEL_SELECTED_INDEX);
            System.out.println("Start-Position des Ziel-Knotens (relativ zur HBox): " + targetNodeStartX);
            System.out.println("Berechnetes Animationsziel (targetX): " + targetX);
            System.out.println("-------------------------------------------------");

            TranslateTransition tt = new TranslateTransition(Duration.seconds(6), rarityContainer); // Speed
            tt.setToX(targetX);
            tt.setInterpolator(javafx.animation.Interpolator.EASE_OUT);

            tt.setOnFinished(event -> {
                System.out.println("Animation beendet! Du hast erhalten: " + card);
                if (spinButton != null) {
                    spinButton.setDisable(false);
                }
            });

            tt.play();
        });
    }

    private Node createRarityNode(int rarityId) {
        Rarity rarity = Rarity.fromSortID(rarityId);
        String imageName = RARITY_IMAGE_MAP.getOrDefault(rarity, "CommonSlot.png");
        String fullPath = IMAGE_PATH + imageName;

        URL imageUrl = getClass().getResource(fullPath);
        if (imageUrl == null) {
            System.err.println("FATAL: Bildressource nicht gefunden: " + fullPath);
            return createErrorNode(imageName);
        }

        Image image = new Image(imageUrl.toExternalForm());
        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(NODE_WIDTH);
        imageView.setFitHeight(NODE_HEIGHT);
        imageView.setPreserveRatio(true);
        imageView.setEffect(new DropShadow(10, Color.BLACK));
        return imageView;
    }

    private Node createErrorNode(String missingImageName) {
        StackPane errorPane = new StackPane();
        errorPane.setPrefSize(NODE_WIDTH, NODE_HEIGHT);
        errorPane.setStyle("-fx-background-color: red; -fx-border-color: white;");
        javafx.scene.control.Label label = new javafx.scene.control.Label("MISSING\n" + missingImageName);
        label.setWrapText(true);
        label.setTextFill(Color.WHITE);
        errorPane.getChildren().add(label);
        return errorPane;
    }

    private List<Integer> getRarityVisualList(Rarity selectedRarity) {
        List<Integer> weightedList = Arrays.stream(Rarity.values())
                .flatMap(rarity -> IntStream.range(0, (int) Math.round(rarity.getWeight() * CAROUSEL_ELEMENT_SIZE))
                        .mapToObj(i -> rarity.getSortID()))
                .collect(Collectors.toCollection(ArrayList::new));

        while (weightedList.size() < CAROUSEL_ELEMENT_SIZE) {
            weightedList.add(Rarity.COMMON.getSortID());
        }

        Collections.shuffle(weightedList);
        weightedList.set(CAROUSEL_SELECTED_INDEX, selectedRarity.getSortID());
        return weightedList;
    }

    private Card getRandomCard() {
        boolean anyCardIsAvailable = cards.stream()
                .anyMatch(card -> card.getAmount() < card.getRarity().getMaxAmount());

        if (!anyCardIsAvailable) {
            System.err.println("WARNUNG: Alle Karten im Spiel haben ihr Maximum erreicht. Keine Ziehung möglich.");
            return null;
        }

        while (true) {
            Rarity rarity = Rarity.getRandom();
            System.out.println("Versuche Ziehung für Seltenheit: " + rarity.name());

            List<Card> eligibleCards = cards.stream()
                    .filter(card -> card.getRarity() == rarity && card.getAmount() < card.getRarity().getMaxAmount())
                    .toList();

            if (!eligibleCards.isEmpty()) {
                return eligibleCards.get(ThreadLocalRandom.current().nextInt(eligibleCards.size()));
            }

            System.err.println("Keine verfügbare Karte für " + rarity.name() + " gefunden. Neuer Versuch...");
        }
    }
}