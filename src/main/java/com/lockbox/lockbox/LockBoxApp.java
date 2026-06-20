package com.lockbox.lockbox;

import javafx.application.Application;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

@SpringBootApplication
public class LockBoxApp extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(
                getClass().getResource("/com/lockbox/lockbox/login-view.fxml")
        );

        Scene scene = new Scene(fxmlLoader.load(), 420, 520);
        stage.setTitle("LockBox");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}