
package org.example.demo3.model.service;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import static org.example.demo3.model.constants.Config.*;

public class NavigationService {

    private final Scene mainScene;
    private final Stage primaryStage;
    private final String menuCssPath;

    private static final Set<String> FXMLS_WITH_MENU_CSS = Set.of(
            FXML_INVENTORY_PATH,
            FXML_EDITOR_PATH,
            FXML_MAINMENU_PATH,
            FXML_FACTION_TAB_PATH,
            FXML_UNLOCKER_PATH
    );


    public NavigationService(Scene mainScene, Stage primaryStage) {
        this.mainScene = Objects.requireNonNull(mainScene, "Main scene cannot be null");
        this.primaryStage = Objects.requireNonNull(primaryStage, "Primary stage cannot be null");
        this.menuCssPath = CSS_PATH;
    }

    public void navigateTo(String fxmlPath, String title) {
        navigateTo(fxmlPath, title, null);
    }

    public <T> void navigateTo(String fxmlPath, String title, ControllerInitializer<T> controllerSetup) {
        try {
            System.out.println("Navigating to: " + fxmlPath);
            FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(getClass().getResource(fxmlPath), "FXML not found: " + fxmlPath));
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
            applyStylesIfNeeded(fxmlPath);

            primaryStage.setTitle(title);

            System.out.println("Navigation to " + fxmlPath + " successful.");
        } catch (IOException e) {
            System.err.println("Failed to load FXML: " + fxmlPath + " - " + e.getMessage());
            e.printStackTrace();
        } catch (NullPointerException e) {
            System.err.println("Failed to find resource: " + fxmlPath + " - " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void applyStylesIfNeeded(String fxmlPath) {
        if (menuCssPath == null || menuCssPath.isEmpty()) {
            System.err.println("WARNUNG: Pfad zur CSS-Datei (menuCssPath) ist nicht konfiguriert.");
            return;
        }

        String cssUrl;

        try {
            cssUrl = Objects.requireNonNull(getClass().getResource(menuCssPath), "CSS not found" + menuCssPath).toExternalForm();
        } catch (NullPointerException e) {
            System.err.println("WARNING: CSS-Datei nicht gefunden unter: " + menuCssPath);
            return;
        }

        mainScene.getStylesheets().remove(cssUrl);

        if (FXMLS_WITH_MENU_CSS.contains(fxmlPath)) {
            mainScene.getStylesheets().add(cssUrl);
            System.out.println("Applied CSS '" + menuCssPath + "' to: " + fxmlPath);
        } else {
            System.out.println("Ensured CSS '" + menuCssPath + "' is not applied to: " + fxmlPath);
        }
    }

    @FunctionalInterface
    public interface ControllerInitializer<T> {
        void initialize(T controller);
    }
}