package com.example.chronopanthers;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class ChronoPanthers extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(ChronoPanthers.class.getResource("loginPage.fxml"));
        //FXMLLoader fxmlLoader = new FXMLLoader(ChronoPanthers.class.getResource("productivity.fxml"));
        Parent root = fxmlLoader.load();
        //Productivity productivity = fxmlLoader.getController();
        //productivity.setCurrentUsername("sahishnu");
        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/com/example/chronopanthers/loginPage.css").toExternalForm());
        //scene.getStylesheets().add(getClass().getResource("/com/example/chronopanthers/taskManager.css").toExternalForm());
        stage.setTitle("Login Page");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}