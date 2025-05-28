package org.example.demo3.view;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.example.demo3.model.service.GameService;
import org.example.demo3.model.service.NavigationService;

import static org.example.demo3.model.constants.InventoryConstants.*;

import java.io.IOException;

public class HelloApplication extends Application {
    private static NavigationService navigationService;

    public static NavigationService getNavigationService() { return navigationService; }


    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws IOException {
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

        // CSS
        navigationService.applyStylesIfNeeded(FXML_MAINMENU_PATH);

        stage.setTitle("GWENT");
        stage.setScene(scene);
        stage.show();
    }

    /*
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("/org/example/demo3/hello-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 800, 600);
        GameService service = new GameService();
        service.newGame();
        stage.setTitle("Gwent");
        stage.setScene(scene);
        stage.show();
    }
     */
}