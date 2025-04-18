package org.example.demo3.ui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.example.demo3.service.GameServiceImpl;

import java.io.IOException;

public class HelloApplication extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("/org/example/demo3/hello-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 800, 600);
        GameServiceImpl service = new GameServiceImpl();
        service.initializeGame();
        stage.setTitle("Card Game");
        stage.setScene(scene);
        stage.show();
    }
}