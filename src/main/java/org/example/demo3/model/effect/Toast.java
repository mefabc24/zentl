package org.example.demo3.model.effect;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.control.Label;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.util.Duration;

import static org.example.demo3.model.constants.Config.TOAST_DURATION_SECONDS;

public class Toast {

    public static void makeText(Stage ownerStage, String message, int durationMillis) {
        Popup popup = new Popup();

        Label label = new Label(message);
        label.getStyleClass().add("toast-label");

        popup.getContent().add(label);
        popup.setAutoFix(true);
        popup.setAutoHide(true);
        popup.setHideOnEscape(true);


        popup.setOnShown(event -> {
            double x = ownerStage.getX() + (ownerStage.getWidth() / 2) - (label.prefWidth(-1) / 2);
            double y = ownerStage.getY() + ownerStage.getHeight() - label.prefHeight(-1) - 50;
            popup.setX(x);
            popup.setY(y);
        });

        popup.show(ownerStage);

        Timeline timeline = new Timeline(new KeyFrame(
                Duration.millis(durationMillis),
                ae -> popup.hide()));
        timeline.play();
    }

    @SuppressWarnings("unused")
    public static void makeText(Stage ownerStage, String message) {
        makeText(ownerStage, message, TOAST_DURATION_SECONDS * 1000); // Umwandlung in ms
    }

}