package org.example.demo3.model.effect;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.control.Label;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.util.Duration;

import static org.example.demo3.model.constants.Config.TOAST_DURATION_SECONDS;

public class Toast {

    public static void makeText(Stage ownerStage, String message) {
        Popup popup = new Popup();

        Label label = new Label(message);
        label.setStyle(
                "-fx-background-color: rgba(80, 80, 80, 0.85);" +
                        "-fx-text-fill: #F0F0F0;" +
                        "-fx-font-size: 15px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-padding: 12px 25px;" +
                        "-fx-background-radius: 20px;" +
                        "-fx-border-radius: 20px;" +
                        "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.5), 10, 0, 0, 5);"
        );

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
                Duration.millis(TOAST_DURATION_SECONDS * 1000), // Umwandlung in MS
                ae -> popup.hide()));
        timeline.play();
    }
}