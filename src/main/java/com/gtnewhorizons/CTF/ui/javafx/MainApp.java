package com.gtnewhorizons.CTF.ui.javafx;

import java.io.IOException;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            // Load the FXML file and set up the scene
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/MainLayout.fxml"));
            Parent root = loader.load();

            // Set the primary scene
            Scene scene = new Scene(root);
            primaryStage.setScene(scene);
            primaryStage.setTitle("Test Application");

            // Get the controller and set the stage
            MainController controller = loader.getController();
            controller.setPrimaryStage(primaryStage); // Pass the primary stage

            // Show the stage
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace(); // Log the error for debugging
        }
    }

}
