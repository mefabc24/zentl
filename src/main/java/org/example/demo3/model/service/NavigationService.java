
package org.example.demo3.model.service;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import static org.example.demo3.model.constants.InventoryConstants.*;

public class NavigationService {

    private Scene mainScene;
    private Stage primaryStage;
    private final String menuCssPath;

    private final Set<String> fxmlsWithMenuCss = new HashSet<>();


    public NavigationService(Scene mainScene, Stage primaryStage) {
        this.mainScene = Objects.requireNonNull(mainScene, "Main scene cannot be null");
        this.primaryStage = Objects.requireNonNull(primaryStage, "Primary stage cannot be null");

        this.menuCssPath = CSS_PATH;

        fxmlsWithMenuCss.add(FXML_INVENTORY_PATH);
        fxmlsWithMenuCss.add(FXML_EDITOR_PATH);
        fxmlsWithMenuCss.add(FXML_MAINMENU_PATH);
        fxmlsWithMenuCss.add(FXML_FACTION_TAB_PATH);
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
            applyStylesIfNeeded(fxmlPath);

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

    public void applyStylesIfNeeded(String fxmlPath) {
        if (menuCssPath == null || menuCssPath.isEmpty()) {
            System.err.println("WARNUNG: Pfad zur CSS-Datei (menuCssPath) ist nicht konfiguriert.");
            return;
        }

        String cssUrl = null;
        try {
            cssUrl = getClass().getResource(menuCssPath).toExternalForm();
        } catch (NullPointerException e) {
            System.err.println("WARNUNG: CSS-Datei nicht gefunden unter: " + menuCssPath);
            return;
        }

        mainScene.getStylesheets().remove(cssUrl);

        if (fxmlsWithMenuCss.contains(fxmlPath)) {
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