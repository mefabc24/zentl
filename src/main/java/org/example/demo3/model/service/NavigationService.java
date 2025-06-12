package org.example.demo3.model.service;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static org.example.demo3.model.constants.Config.FXML_GAME_PATH;
import static org.example.demo3.model.constants.Config.FXML_GAME_SETUP_PATH;
import static org.example.demo3.model.constants.Config.FXML_GAME_MODE_SELECTION_PATH;

public class NavigationService {

    private final StackPane mainContentPane;
    private final Stage primaryStage;

    private final Map<String, Parent> viewCache = new HashMap<>();
    private final Map<String, Object> controllerCache = new HashMap<>();
    private final Set<String> initializedControllers = new HashSet<>();
    
    private static final Set<String> NON_CACHEABLE_VIEWS = Set.of(
            FXML_GAME_PATH,
            FXML_GAME_SETUP_PATH,
            FXML_GAME_MODE_SELECTION_PATH
    );

    private Object currentController;
    private String currentFxmlPath;

    public NavigationService(StackPane mainContentPane, Stage primaryStage) {
        this.mainContentPane = Objects.requireNonNull(mainContentPane, "Main content pane cannot be null");
        this.primaryStage = Objects.requireNonNull(primaryStage, "Primary stage cannot be null");
    }

    public void preLoadView(String fxmlPath) {
        if (viewCache.containsKey(fxmlPath) || NON_CACHEABLE_VIEWS.contains(fxmlPath)) {
            return;
        }

        new Thread(() -> {
            System.out.println("[BACKGROUND] Starting preload for: " + fxmlPath);
            try {
                FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(getClass().getResource(fxmlPath)));
                Platform.runLater(() -> {
                    try {
                        Parent view = loader.load();
                        Object controller = loader.getController();

                        viewCache.put(fxmlPath, view);
                        controllerCache.put(fxmlPath, controller);
                        System.out.println("[UI THREAD] Finished preload and cached: " + fxmlPath);
                    } catch (IOException e) {
                        System.err.println("Error during UI-thread part of preload for: " + fxmlPath);
                        e.printStackTrace();
                    }
                });
            } catch (Exception e) {
                System.err.println("Error during background part of preload for: " + fxmlPath);
                e.printStackTrace();
            }
        }).start();
    }

    public void navigateTo(String fxmlPath, String title) {
        navigateTo(fxmlPath, title, null);
    }

    public <T> void navigateTo(String fxmlPath, String title, ControllerInitializer<T> controllerSetup) {
        try {
            if (currentFxmlPath != null && NON_CACHEABLE_VIEWS.contains(currentFxmlPath)) {
                if (currentController instanceof Cleanable) {
                    ((Cleanable) currentController).cleanup();
                }
            }

            Parent view;
            Object controller;
            boolean isCacheable = !NON_CACHEABLE_VIEWS.contains(fxmlPath);

            if (isCacheable && viewCache.containsKey(fxmlPath)) {
                System.out.println("Loading view from cache: " + fxmlPath);
                view = viewCache.get(fxmlPath);
                controller = controllerCache.get(fxmlPath);
            } else {
                System.out.println("Loading view from FXML: " + fxmlPath);
                FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(getClass().getResource(fxmlPath), "FXML not found: " + fxmlPath));
                view = loader.load();
                controller = loader.getController();

                if (isCacheable) {
                    viewCache.put(fxmlPath, view);
                    controllerCache.put(fxmlPath, controller);
                }
            }

            if (controllerSetup != null && (!isCacheable || !initializedControllers.contains(fxmlPath))) {
                @SuppressWarnings("unchecked")
                T typedController = (T) controller;
                if (typedController != null) {
                    System.out.println("Running initialization for controller of: " + fxmlPath);
                    controllerSetup.initialize(typedController);
                    if (isCacheable) {
                        initializedControllers.add(fxmlPath);
                    }
                } else {
                    System.err.println("Warning: Controller for " + fxmlPath + " is null during setup.");
                }
            }

            mainContentPane.getChildren().setAll(view);

            this.currentController = controller;
            this.currentFxmlPath = fxmlPath;

            primaryStage.setTitle(title);
            System.out.println("Navigation to " + fxmlPath + " successful.");

        } catch (IOException | NullPointerException e) {
            System.err.println("Navigation failed for FXML: " + fxmlPath);
            e.printStackTrace();
        }
    }

    @FunctionalInterface
    public interface ControllerInitializer<T> {
        void initialize(T controller);
    }
}