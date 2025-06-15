package org.example.demo3.view;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.example.demo3.model.service.NavigationService;
import org.example.demo3.model.service.SoundService;
import javafx.scene.text.Font;
import java.io.InputStream;
import java.util.Objects;

import static org.example.demo3.model.constants.Config.*;

import java.io.IOException;

public class HelloApplication extends Application {
    private static NavigationService navigationService;

    public static NavigationService getNavigationService() { return navigationService; }

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws IOException {
        try (InputStream CodaStream = getClass().getResourceAsStream("/org/example/demo3/assets/fonts/Coda-Regular.ttf")) {
            Font.loadFont(CodaStream, 10);
        } catch (Exception e) {
            System.err.println("Fehler beim Laden der Schriftarten aufgetreten.");
            e.printStackTrace();
        }

        FXMLLoader mainLoader = new FXMLLoader(HelloApplication.class.getResource("/org/example/demo3/main-view.fxml"));
        StackPane root = mainLoader.load();
        MainViewController mainViewController = mainLoader.getController();

        Scene scene = new Scene(root, 1920, 1080);

        try {
            String cssPath = Objects.requireNonNull(getClass().getResource(CSS_PATH)).toExternalForm();
            scene.getStylesheets().add(cssPath);
            System.out.println("Successfully applied global stylesheet: " + cssPath);
        } catch (NullPointerException e) {
            System.err.println("FATAL: Global stylesheet not found at: " + CSS_PATH);
            e.printStackTrace();
        }

        navigationService = new NavigationService(mainViewController.getMainContentPane(), stage);

        // Perform the initial navigation to the main menu
        navigationService.navigateTo(FXML_MAINMENU_PATH, "GWENT", (MainMenuController controller) -> {
            controller.setNavigationService(navigationService);
            controller.setStage(stage);
        });

        System.out.println("Kicking off preload for Inventory...");
        navigationService.preLoadView(FXML_DECKBUILDER_PATH);

        SoundService.getInstance().startMenuMusic();
        stage.setTitle("GWENT");
        stage.setScene(scene);
        stage.show();
    }
}