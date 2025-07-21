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

        // IMPORTANT: Activate the controller to sync UI with timer state
        controller.onControllerActivated();

        switchScene(loader, root, "Pomodoro Timer", "/com/example/chronopanthers/timer.css", event);
    }

    @FXML
    private void goToTaskManager(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("taskManager.fxml"));
        Parent root = loader.load();

        TaskManager controller = loader.getController();
        if (currentUsername != null) {
            controller.setCurrentUser(currentUsername);
        }

        switchScene(loader, root, "Task Manager", "/com/example/chronopanthers/taskManager.css", event);
    }

    @FXML
    private void goToAIAgent(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("aiAgent.fxml"));
        Parent root = loader.load();

        AIAgentController controller = loader.getController();
        if (currentUsername != null) {
            controller.setCurrentUser(currentUsername);
        }

        switchScene(loader, root, "AI Study Assistant", "/com/example/chronopanthers/aiAgent.css", event);
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

        switchScene(loader, root, "Productivity Tracker", "/com/example/chronopanthers/productivity.css", event);
    }

    @FXML
    private void logout(ActionEvent event) throws IOException {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Logout");
        alert.setHeaderText("You're about to logout!");
        alert.setContentText("Have you completed all your work?");

        if(alert.showAndWait().get() == ButtonType.OK){
            FXMLLoader loader = new FXMLLoader(getClass().getResource("loginPage.fxml"));
            Parent root = loader.load();
            switchScene(loader, root, "Login Page", "/com/example/chronopanthers/loginPage.css", event);
        }
    }

    private void switchScene(FXMLLoader loader, Parent root, String title, String cssFile, ActionEvent event) {
        try {
            Stage stage = getStageFromEvent(event);
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource(cssFile).toExternalForm());
            stage.setTitle(title);
            stage.setScene(scene);
            stage.setResizable(false);
            stage.show();

            Object controller = loader.getController();
            if (controller instanceof Controller baseController) {
                baseController.setStage(stage);
            } else if (controller instanceof TaskManager tm) {
                tm.setStage(stage);
            } else if (controller instanceof AIAgentController ai) {
                ai.setStage(stage);
            } else if (controller instanceof Productivity prod) {
                prod.setStage(stage);
            }
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
}