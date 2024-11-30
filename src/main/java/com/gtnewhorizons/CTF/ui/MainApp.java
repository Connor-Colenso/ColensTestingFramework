package com.gtnewhorizons.CTF.ui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class MainApp extends Application {

    @Override
    public void start(Stage stage) {
        try {
            // Load the FXML file and set up the scene
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/MainLayout.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("JavaFX FXML Example");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
