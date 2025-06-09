package org.example.demo3.model.constants;

import javafx.geometry.Pos;

public final class Config {

    private Config() {}

    // UI Constants
    public static final Pos FLOWPANE_ALIGNMENT_ALL_CARDS = Pos.CENTER_LEFT;
    public static final Pos FLOWPANE_ALIGNMENT_SELECTED_CARDS = Pos.CENTER;

    // Card Dimensions
    public static final double CARD_PREVIEW_WIDTH = 207;
    public static final double CARD_PREVIEW_HEIGHT = 307;

    // Selected Card Dimensions
    public static final double SELECTED_CARD_PREVIEW_WIDTH = 172;
    public static final double SELECTED_CARD_PREVIEW_HEIGHT = 255;

    // ScrollPane Constants
    public static final double SCROLL_SPEED_MULTIPLIER = 3.0;

    // Game Logic Constants
    public static final int MAX_SELECTION = 15;
    public static final int MIN_SELECTION = MAX_SELECTION;
    public static final int RANDOMIZER_CARD_AMOUNT = MAX_SELECTION;

    // Effect Constants
    public static final int TOAST_DURATION_SECONDS = 2;

    // Unlock System
    public static final int CAROUSEL_ELEMENT_SIZE = 100;
    public static final int CAROUSEL_SELECTED_INDEX = 96;

    // File Paths
    public static final String FXML_PATH = "/org/example/demo3/";
    public static final String RESOURCE_PATH = "/org/example/demo3/";
    public static final String IMAGE_PATH = RESOURCE_PATH + "assets/";

    public static final String LOCKED_IMAGE_OVERLAY = IMAGE_PATH + "LockedOverlay.png";

    public static final String FXML_EDITOR_PATH = FXML_PATH + "Editor.fxml";
    public static final String FXML_FACTION_TAB_PATH = FXML_PATH + "faction-tab-content.fxml";
    public static final String FXML_INVENTORY_PATH = FXML_PATH + "inventory-view.fxml";
    public static final String FXML_MAINMENU_PATH = FXML_PATH + "MainMenu.fxml";
    public static final String FXML_UNLOCKER_PATH = FXML_PATH + "card-unlock.fxml";
    public static final String FXML_GAME_SETUP_PATH = FXML_PATH + "GameSetup.fxml";
    public static final String FXML_GAME_PATH = FXML_PATH + "hello-view.fxml";
    public static final String FXML_GAME_MODE_SELECTION_PATH = FXML_PATH + "GameModeSelection.fxml";
    
    public static final String JSON_PATH = RESOURCE_PATH + "cards.json";
    public static final String CSS_PATH = RESOURCE_PATH + "styles.css";
    
}