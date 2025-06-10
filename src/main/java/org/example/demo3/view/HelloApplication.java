package org.example.demo3.view;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.example.demo3.model.service.NavigationService;
import org.example.demo3.model.service.SoundService;
import javafx.scene.text.Font;
import java.io.InputStream;

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

        try (
                InputStream arialStream = getClass().getResourceAsStream("/org/example/demo3/assets/fonts/ariblk.ttf");
                InputStream impactStream = getClass().getResourceAsStream("/org/example/demo3/assets/fonts/impact.ttf")
        ) {
                Font.loadFont(arialStream, 10);
                Font.loadFont(impactStream, 10);
        } catch (Exception e) {
            System.err.println("Fehler beim Laden der Schriftarten aufgetreten.");
            e.printStackTrace();
        }

        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource(FXML_MAINMENU_PATH));
        Scene scene = new Scene(fxmlLoader.load(), 1920, 1080);

        navigationService = new NavigationService(scene, stage);

        MainMenuController mainMenuController = fxmlLoader.getController();
        if (mainMenuController != null) {
            mainMenuController.setNavigationService(navigationService);
            mainMenuController.setStage(stage);
        } else {
            System.err.println("MainMenuController konnte nicht geladen werden!");
        }

        // play the fab menu theme
        SoundService.getInstance().startMenuMusic();

        navigationService.applyStylesIfNeeded(FXML_MAINMENU_PATH);
        stage.setTitle("GWENT");
        stage.setScene(scene);
        stage.show();

        // CSS
        navigationService.applyStylesIfNeeded(FXML_MAINMENU_PATH);

        stage.setTitle("GWENT");
        stage.setScene(scene);
        stage.show();
    }
}