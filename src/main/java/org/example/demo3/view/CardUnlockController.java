package org.example.demo3.view;

import javafx.animation.*;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label; // Wichtig, dass dieser Import da ist
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
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
    // FXML-Elemente
    @FXML private StackPane carouselPane;
    @FXML private HBox rarityContainer;
    @FXML private Button spinButton;
    @FXML private Rectangle indicator;
    @FXML private StackPane finalCardPane;
    @FXML private Label newCardLabel; // NEUE Referenz

    // ... (restliche Klassenvariablen bleiben gleich)
    private NavigationService navigationService;
    private final CardRepository cardRepository = CardRepository.getInstance();
    private final List<Card> cards = cardRepository.getUnlockableCards();
    private int revealClicks = 0;
    private static final double NODE_WIDTH = 300.0;
    private static final double NODE_HEIGHT = 300.0;
    private static final double SPACING = 30.0;
    private static final Map<Rarity, String> RARITY_IMAGE_MAP = new EnumMap<>(Rarity.class);

    static {
        // ... (Map-Initialisierung bleibt gleich)
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
        indicator.setVisible(false);
    }

    @FXML
    public void generateCard() {
        // --- VEREINFACHUNG ---
        // UI und Zustände für einen neuen Spin zurücksetzen
        indicator.setVisible(true);
        finalCardPane.getChildren().clear();
        newCardLabel.setVisible(false); // Label am Anfang immer ausblenden
        revealClicks = 0;

        // ... (restlicher Code zum Starten der Animation bleibt gleich) ...
        if (spinButton != null) spinButton.setDisable(true);
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
            TranslateTransition tt = new TranslateTransition(Duration.seconds(8), rarityContainer);
            tt.setToX(targetX);
            tt.setInterpolator(Interpolator.EASE_OUT);
            tt.setOnFinished(event -> {
                System.out.println("Karussell gestoppt! Starte Reveal-Sequenz für: " + card);
                startRevealSequence(card);
            });
            tt.play();
        });
    }

    // --- startRevealSequence bleibt unverändert ---
    private void startRevealSequence(Card wonCard) {
        indicator.setVisible(false);
        Node winningNode = rarityContainer.getChildren().get(CAROUSEL_SELECTED_INDEX);

        for (Node node : rarityContainer.getChildren()) {
            if (node != winningNode) {
                FadeTransition ft = new FadeTransition(Duration.millis(300), node);
                ft.setToValue(0);
                ft.play();
            }
        }
        winningNode.setOpacity(1.0);
        ScaleTransition press = new ScaleTransition(Duration.millis(100), winningNode);
        press.setToX(0.9);
        press.setToY(0.9);
        ScaleTransition release = new ScaleTransition(Duration.millis(100), winningNode);
        release.setToX(1.0);
        release.setToY(1.0);
        SequentialTransition clickAnimation = new SequentialTransition(press, release);
        EventHandler<MouseEvent> clickHandler = new EventHandler<>() {
            @Override
            public void handle(MouseEvent event) {
                revealClicks++;
                System.out.println("Reveal-Klick #" + revealClicks);
                clickAnimation.playFromStart();
                if (revealClicks >= 3) {
                    winningNode.removeEventHandler(MouseEvent.MOUSE_CLICKED, this);
                    revealFinalCard(wonCard, winningNode);
                }
            }
        };
        winningNode.addEventHandler(MouseEvent.MOUSE_CLICKED, clickHandler);
    }

    // --- `revealFinalCard` wird drastisch vereinfacht ---
    private void revealFinalCard(Card card, Node rarityNode) {
        System.out.println("Enthülle finale Karte: " + card.getName());

        Image finalCardImageObject = card.getImage();
        if (finalCardImageObject == null) {
            System.err.println("FATAL: Konnte das finale Kartenbild für '" + card.getName() + "' nicht laden.");
            if (spinButton != null) spinButton.setDisable(false);
            rarityNode.setOpacity(0);
            return;
        }

        ImageView cardImage = new ImageView(finalCardImageObject);
        cardImage.setFitHeight(NODE_HEIGHT * 1.5);
        cardImage.setPreserveRatio(true);
        cardImage.setEffect(new DropShadow(20, Color.BLACK));
        cardImage.setOpacity(0);

        // Füge nur noch das Kartenbild zum finalCardPane hinzu
        finalCardPane.getChildren().add(cardImage);

        // Animationen für das Ausblenden des Slots
        FadeTransition ftOut = new FadeTransition(Duration.millis(400), rarityNode);
        ftOut.setToValue(0);
        ScaleTransition stOut = new ScaleTransition(Duration.millis(400), rarityNode);
        stOut.setToX(0.1);
        stOut.setToY(0.1);

        // Animationen für das Einblenden der Karte
        FadeTransition ftInCard = new FadeTransition(Duration.millis(500), cardImage);
        ftInCard.setToValue(1);
        ScaleTransition stInCard = new ScaleTransition(Duration.millis(500), cardImage);
        stInCard.setFromX(0.1);
        stInCard.setFromY(0.1);
        stInCard.setToX(1.0);
        stInCard.setToY(1.0);
        stInCard.setInterpolator(Interpolator.EASE_OUT);

        // Starte alle Animationen
        ftOut.play();
        stOut.play();
        ftInCard.play();
        stInCard.play();

        // Wenn die Karte neu ist, zeige das separate Label an
        if (card.getAmount() <= 0) {
            System.out.println("NEW CARD");
            newCardLabel.setVisible(true); // Einfach sichtbar machen

            // Optional: Ein sanfter Fade-In für das Label
            FadeTransition ftInLabel = new FadeTransition(Duration.millis(500), newCardLabel);
            ftInLabel.setFromValue(0);
            ftInLabel.setToValue(1);
            ftInLabel.setDelay(Duration.millis(300));
            ftInLabel.play();
        }

        // Aktionen nach dem Ende der Kartenanimation
        stInCard.setOnFinished(event -> {
            card.setAmount(card.getAmount() + 1);
            System.out.println("Neuer Amount für '" + card.getName() + "': " + card.getAmount());
            if (spinButton != null) {
                spinButton.setDisable(false);
            }
        });
    }

    // --- Alle restlichen Methoden (createRarityNode, etc.) bleiben unverändert ---
    private Node createRarityNode(int rarityId) { //... (keine Änderung)
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
    private Node createErrorNode(String missingImageName) { //... (keine Änderung)
        StackPane errorPane = new StackPane();
        errorPane.setPrefSize(NODE_WIDTH, NODE_HEIGHT);
        errorPane.setStyle("-fx-background-color: red; -fx-border-color: white;");
        Label label = new Label("MISSING\n" + missingImageName);
        label.setWrapText(true);
        label.setTextFill(Color.WHITE);
        errorPane.getChildren().add(label);
        return errorPane;
    }
    private List<Integer> getRarityVisualList(Rarity selectedRarity) { //... (keine Änderung)
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
    private Card getRandomCard() { //... (keine Änderung)
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