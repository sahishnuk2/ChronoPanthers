package com.example.chronopanthers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.stage.Stage;

import java.io.IOException;

public class NavigationController {
    @FXML
    private Label usernameLabel;

    private String currentUsername;

    private Stage stage;

    public void setCurrentUser(String username) {
        this.currentUsername = username;
        System.out.println("NavigationController: Setting username to: " + username);
        if (usernameLabel != null) {
            usernameLabel.setText("Welcome, " + username);
        }
    }

    @FXML
    private void goToTimer(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("timer.fxml"));
        Parent root = loader.load();

        Controller controller = loader.getController();
        if (currentUsername != null) {
            controller.setCurrentUser(currentUsername);
            // Also initialize the navigation after setting username
            controller.initializeNavigation();
        }

        switchScene(root, "Pomodoro Timer", "/com/example/chronopanthers/timer.css", event);
    }

    @FXML
    private void goToTaskManager(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("taskManager.fxml"));
        Parent root = loader.load();

        TaskManager controller = loader.getController();
        if (currentUsername != null) {
            controller.setCurrentUser(currentUsername);
        }

        switchScene(root, "Task Manager", "/com/example/chronopanthers/taskManager.css", event);
    }

    @FXML
    private void goToAIAgent(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("aiAgent.fxml"));
        Parent root = loader.load();

        AIAgentController controller = loader.getController();
        if (currentUsername != null) {
            controller.setCurrentUser(currentUsername);
        }

        switchScene(root, "AI Study Assistant", "/com/example/chronopanthers/aiAgent.css", event);
    }

    @FXML
    private void goToStats(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("productivity.fxml"));
        Parent root = loader.load();

        Productivity controller = loader.getController();
        if (currentUsername != null) {
            controller.setCurrentUsername(currentUsername);
            // Also initialize the navigation after setting username
            controller.initializeNavigation();
        }

        switchScene(root, "Productivity Tracker", "/com/example/chronopanthers/productivity.css", event);
    }

    @FXML
    private void logout(ActionEvent event) throws IOException {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Logout");
        alert.setHeaderText("You're about to logout!");
        alert.setContentText("Have you completed all your work?");

        if(alert.showAndWait().get() == ButtonType.OK){
            Parent root = FXMLLoader.load(getClass().getResource("loginPage.fxml"));
            switchScene(root, "Login Page", "/com/example/chronopanthers/loginPage.css", event);
        }
    }

    private void switchScene(Parent root, String title, String cssFile, ActionEvent event) {
        try {
            Stage stage = getStageFromEvent(event);
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource(cssFile).toExternalForm());
            stage.setTitle(title);
            stage.setScene(scene);
            stage.setResizable(false);
            stage.show();
        } catch (Exception e) {
            System.err.println("Error switching scenes: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private Stage getStageFromEvent(ActionEvent event) {
        Object source = event.getSource();
        if (source instanceof MenuItem) {
            MenuItem menuItem = (MenuItem) source;
            return (Stage) menuItem.getParentPopup().getOwnerWindow();
        } else if (source instanceof Node) {
            Node node = (Node) source;
            return (Stage) node.getScene().getWindow();
        }
        throw new IllegalArgumentException("Unable to determine stage from event source: " + source.getClass());
    }

    public void setStage(Stage stage) {
        this.stage = stage;

        this.stage.setOnCloseRequest(event -> {
            event.consume(); // prevent window from closing
            handleLogoutRequest(); // show the confirmation dialog
        });
    }

    private void handleLogoutRequest() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Logout");
        alert.setHeaderText("You're about to logout!");
        alert.setContentText("Have you completed all your work?");

        if (alert.showAndWait().get() == ButtonType.OK) {
            TimerManager.getInstance().reset();

            try {
                Parent root = FXMLLoader.load(getClass().getResource("loginPage.fxml"));
                Scene scene = new Scene(root);
                scene.getStylesheets().add(getClass().getResource("/com/example/chronopanthers/loginPage.css").toExternalForm());

                stage.setTitle("Login Page");
                stage.setScene(scene);
                stage.setResizable(false);
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}