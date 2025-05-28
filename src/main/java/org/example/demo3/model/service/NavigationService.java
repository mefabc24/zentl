
package org.example.demo3.model.service;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class NavigationService {

    private Scene mainScene;
    private Stage primaryStage;


    public NavigationService(Scene mainScene, Stage primaryStage) {
        this.mainScene = Objects.requireNonNull(mainScene, "Main scene cannot be null");
        this.primaryStage = Objects.requireNonNull(primaryStage, "Primary stage cannot be null");
    }

    public void navigateTo(String fxmlPath, String title) {
        navigateTo(fxmlPath, title, null);
    }

    public <T> void navigateTo(String fxmlPath, String title, ControllerInitializer<T> controllerSetup) {
        try {
            System.out.println("Navigating to: " + fxmlPath);
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent newRoot = loader.load();

            if (controllerSetup != null) {
                T controller = loader.getController();
                if (controller != null) {
                    controllerSetup.initialize(controller);
                } else {
                    System.err.println("Warning: Controller for " + fxmlPath + " is null.");
                }
            }

            mainScene.setRoot(newRoot);

            if (primaryStage != null) {
                primaryStage.setTitle(title);
            }
            System.out.println("Navigation to " + fxmlPath + " successful.");
        } catch (IOException e) {
            System.err.println("Failed to load FXML: " + fxmlPath + " - " + e.getMessage());
            e.printStackTrace();
        } catch (NullPointerException e) {
            System.err.println("Failed to find FXML resource: " + fxmlPath + " - " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FunctionalInterface
    public interface ControllerInitializer<T> {
        void initialize(T controller);
    }
}