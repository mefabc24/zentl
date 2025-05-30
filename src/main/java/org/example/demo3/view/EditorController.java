package org.example.demo3.view;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.example.demo3.model.enums.CardType;
import org.example.demo3.model.enums.Faction;
import org.example.demo3.model.enums.Rarity;
import org.example.demo3.model.enums.RowType;
import org.example.demo3.model.logic.CardRepository;
import org.example.demo3.model.service.NavigationService;

import java.util.function.UnaryOperator;

import static org.example.demo3.model.constants.Config.*;

public class EditorController {

    @FXML private TextField powerText;
    @FXML private TextField amountText;
    @FXML private TextField nameText;
    @FXML private TextField descText;
    @FXML private TextField imagePathText;
    @FXML private ComboBox<RowType> rowTypeComboBox;
    @FXML private ComboBox<CardType> cardTypeComboBox;
    @FXML private ComboBox<Faction> factionComboBox;
    @FXML private ComboBox<Rarity> rarityComboBox;

    @FXML private Button generateButton;
    @FXML private Button backButton;
    @FXML private TextArea outputArea;

    private NavigationService navigationService;
    private final CardRepository cardRepository = new CardRepository();

    public void setNavigationService(NavigationService navigationService) {
        this.navigationService = navigationService;
    }

    @FXML
    private void initialize() {
        rowTypeComboBox.setItems(FXCollections.observableArrayList(RowType.values()));
        cardTypeComboBox.setItems(FXCollections.observableArrayList(CardType.values()));
        factionComboBox.setItems(FXCollections.observableArrayList(Faction.values()));
        rarityComboBox.setItems(FXCollections.observableArrayList(Rarity.values()));

        rowTypeComboBox.setValue(RowType.MELEE);
        cardTypeComboBox.setValue(CardType.UNIT);
        factionComboBox.setValue(Faction.KNIGHTS);
        rarityComboBox.setValue(Rarity.COMMON);

        UnaryOperator<TextFormatter.Change> integerFilter = change -> {
            String newText = change.getControlNewText();
            if (newText.matches("[0-9]*")) {
                return change;
            }
            return null;
        };

        powerText.setTextFormatter(new TextFormatter<>(integerFilter));

        outputArea.setText("Fülle die Felder aus und klicke auf 'Generate JSON'.");
    }

    @FXML
    void handleGenerateAction(ActionEvent event) {
        try {
            int id = cardRepository.getLastID() + 1;

            int power;
            try {
                power = Integer.parseInt(powerText.getText());
            } catch (NumberFormatException e) {
                outputArea.setText("Fehler: Stärke muss eine gültige ganze Zahl sein.");
                return;
            }

            int amount;
            try {
                amount = Integer.parseInt(amountText.getText());

                if (amount < 0) {
                    amount = 0;
                }
            } catch (NumberFormatException e) {
                outputArea.setText("Fehler: Amount muss eine gültige ganze Zahl sein.");
                return;
            }

            String name = nameText.getText();
            if (name.trim().isEmpty()) {
                outputArea.setText("Fehler: Name darf nicht leer sein.");
                return;
            }

            String desc = descText.getText();

            String imagePath = imagePathText.getText();

            RowType rowType = rowTypeComboBox.getValue();
            if (rowType == null) {
                outputArea.setText("Fehler: Bitte einen Reihentyp auswählen.");
                return;
            }

            CardType cardType = cardTypeComboBox.getValue();
            if (cardType == null) {
                outputArea.setText("Fehler: Bitte einen Kartentyp auswählen.");
                return;
            }

            Faction faction = factionComboBox.getValue();
            if (faction == null) {
                outputArea.setText("Fehler: Bitte eine Fraktion auswählen.");
                return;
            }

            Rarity rarity = rarityComboBox.getValue();
            if (rarity == null) {
                outputArea.setText("Fehler: Bitte eine Seltenheit auswählen.");
                return;
            }

            if (cardType == CardType.WEATHER) {
                rowType = RowType.WEATHER;
            } else if (rowType == RowType.WEATHER) {
                cardType = CardType.WEATHER;
            }

            String jsonOutput = String.format(java.util.Locale.US,
                    "{\n" +
                            "    \"id\": %d,\n" +
                            "    \"power\": %d,\n" +
                            "    \"amount\": %d,\n" +
                            "    \"selectedAmount\": %d,\n" +
                            "    \"name\": \"%s\",\n" +
                            "    \"description\": \"%s\",\n" +
                            "    \"imagePath\": \"%s%s\",\n" +
                            "    \"rowType\": \"%s\",\n" +
                            "    \"cardType\": \"%s\",\n" +
                            "    \"faction\": \"%s\",\n" +
                            "    \"rarity\": \"%s\",\n" +
                            "    \"isUnlocked\": \"%b\",\n" +
                            "}",
                    id,
                    power,
                    amount,
                    0,
                    escapeJsonString(name),
                    escapeJsonString(desc),
                    IMAGE_PATH,
                    escapeJsonString(imagePath),
                    rowType.name(),
                    cardType.name(),
                    faction.name(),
                    rarity.name(),
                    true
            );

            outputArea.setText(jsonOutput);
            System.out.println("JSON generiert:\n" + jsonOutput);

        } catch (Exception e) {
            outputArea.setText("Ein unerwarteter Fehler ist aufgetreten: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private String escapeJsonString(String value) {
        if (value == null) return "";
        return value.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\b", "\\b")
                .replace("\f", "\\f")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }


    @FXML
    void handleBackAction(ActionEvent event) {
        if (navigationService != null) {

            navigationService.navigateTo(FXML_MAINMENU_PATH, "Main Menu", (MainMenuController controller) -> {
                controller.setNavigationService(navigationService);
            });
        } else {
            System.err.println("NavigationService ist nicht im EditorController initialisiert.");
            outputArea.setText("Fehler: Navigation zum Hauptmenü nicht möglich.");
        }
    }
}