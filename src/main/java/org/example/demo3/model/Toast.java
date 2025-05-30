package org.example.demo3.model;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.control.Label;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.util.Duration;

public class Toast {

    public static void makeText(Stage ownerStage, String message, int durationMillis) {
        Popup popup = new Popup();

        Label label = new Label(message);
        label.setStyle("-fx-background-color: black; -fx-text-fill: white; -fx-padding: 10px; " +
                "-fx-border-radius: 5px; -fx-background-radius: 5px;");
        label.setOpacity(0.8);

        popup.getContent().add(label);
        popup.setAutoFix(true);
        popup.setAutoHide(true);
        popup.setHideOnEscape(true);

        // Positioniere das Popup unten mittig zum Owner Stage
        double x = ownerStage.getX() + ownerStage.getWidth() / 2 - label.getWidth() / 2;
        double y = ownerStage.getY() + ownerStage.getHeight() - 100;
        popup.show(ownerStage, x, y);

        // Schließe das Popup nach durationMillis
        Timeline timeline = new Timeline(new KeyFrame(
                Duration.millis(durationMillis),
                ae -> popup.hide()));
        timeline.play();
    }
}
