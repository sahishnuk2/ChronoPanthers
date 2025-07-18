package com.example.chronopanthers;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Arc;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import static com.example.chronopanthers.SQliteConnection.*;

public class Controller implements Initializable {
    @FXML
    private Spinner<Integer> workDurationInput, breakDurationInput;
    @FXML
    private Button playButton, pauseButton, resetButton, applySettingsButton;
    @FXML
    private Label timerDisplay, timerState, workSessionsDisplay, breakSessionsDisplay;
    @FXML
    private Circle progressRing;
    @FXML
    private VBox timeBox;
    @FXML
    private Label titleLabel;

    private String currentUsername;
    private int workSessions = 0;
    private int breakSessions = 0;
    private Timeline timeline;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        TimerManager manager = TimerManager.getInstance();

        // Set up duration spinners
        SpinnerValueFactory<Integer> valueFactoryWork = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 60, manager.workTime / 60);
        SpinnerValueFactory<Integer> valueFactoryBreak = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 30, manager.breakTime / 60);
        workDurationInput.setValueFactory(valueFactoryWork);
        breakDurationInput.setValueFactory(valueFactoryBreak);

        // UI update on each tick
        manager.setOnTick(() -> {
            updateDisplay(manager.timeLeft);
            updateProgressRing(manager);
        });

        // Update mode UI on session switch
        manager.setOnModeSwitch(() -> {
            if (manager.isWorkTime) {
                timerState.setText("Work Time");
                timerState.setTextFill(Color.web("#3b82f6"));
                timeBox.setBackground(new Background(new BackgroundFill(Color.web("#f0f9ff"), new CornerRadii(16), Insets.EMPTY)));
                progressRing.setStroke(Color.web("#3b82f6"));
                breakSessions++;
                breakSessionsDisplay.setText(String.valueOf(breakSessions));
                if (currentUsername != null) {
                    updateBreakSession(currentUsername);
                    logBreakSession(currentUsername, manager.breakTime / 60);
                }
            } else {
                timerState.setText("Break Time");
                timerState.setTextFill(Color.web("#10b981"));
                timeBox.setBackground(new Background(new BackgroundFill(Color.web("#f0fdf4"), new CornerRadii(16), Insets.EMPTY)));
                progressRing.setStroke(Color.web("#10b981"));
                workSessions++;
                workSessionsDisplay.setText(String.valueOf(workSessions));
                if (currentUsername != null) {
                    updateWorkSession(currentUsername);
                    logWorkSession(currentUsername, manager.workTime / 60);
                }
            }

            java.awt.Toolkit.getDefaultToolkit().beep(); // play sound
        });

        updateDisplay(manager.timeLeft);
        updateProgressRing(manager);
    }

    public void play() {
        TimerManager.getInstance().start();
        playButton.setStyle("-fx-background-color: #10b981;");
    }

    public void pause() {
        TimerManager.getInstance().pause();
        playButton.setStyle("-fx-background-color: #3b82f6;");
    }

    public void reset() {
        TimerManager.getInstance().reset();
        timerState.setText("Work Time");
        timerState.setTextFill(Color.web("#3b82f6"));
        timeBox.setBackground(new Background(new BackgroundFill(Color.web("#f0f9ff"), new CornerRadii(16), Insets.EMPTY)));
        progressRing.setStroke(Color.web("#3b82f6"));
    }

    public void applySettings() {
        TimerManager manager = TimerManager.getInstance();
        if (!manager.isRunning()) {
            manager.updateDurations(workDurationInput.getValue() * 60, breakDurationInput.getValue() * 60);
            updateDisplay(manager.timeLeft);
            updateProgressRing(manager);
        } else {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Warning");
            alert.setHeaderText("Settings not updated!");
            alert.setContentText("Pause first to apply new settings");
            alert.showAndWait();
        }
    }

    public void setCurrentUser(String username) {
        this.currentUsername = username;
        if (titleLabel != null) {
            titleLabel.setText("Let's Pomodoro, " + username);
        }
        loadSessionCounts();
    }

    private void loadSessionCounts() {
        if (currentUsername != null) {
            int[] counts = SQliteConnection.getSessionCounts(currentUsername);
            workSessions = counts[0];
            breakSessions = counts[1];
            workSessionsDisplay.setText(String.valueOf(workSessions));
            breakSessionsDisplay.setText(String.valueOf(breakSessions));
        }
    }

    public void updateDisplay(int timeLeft) {
        int minutes = timeLeft / 60;
        int seconds = timeLeft % 60;
        timerDisplay.setText(String.format("%02d:%02d", minutes, seconds));
    }

    public void updateProgressRing(TimerManager manager) {
        double progress = (double) (manager.isWorkTime ? manager.workTime - manager.timeLeft : manager.breakTime - manager.timeLeft)
                / (manager.isWorkTime ? manager.workTime : manager.breakTime);

        double circumference = 2 * Math.PI * progressRing.getRadius();
        double visibleLength = progress * circumference;

        progressRing.getStrokeDashArray().clear();
        progressRing.getStrokeDashArray().addAll(visibleLength, circumference);
        progressRing.setRotate(90);
    }

    private Stage stage;
    private Scene scene;

    public void logout(ActionEvent event) throws IOException {

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Logout");
        alert.setHeaderText("You're about to logout!");
        alert.setContentText("Have you completed all your work?");
        if (alert.showAndWait().get() == ButtonType.OK) {
            TimerManager.getInstance().reset();
            Parent root = FXMLLoader.load(getClass().getResource("loginPage.fxml"));
            stage = (Stage) ((MenuItem) event.getSource()).getParentPopup().getOwnerWindow();
            scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/com/example/chronopanthers/loginPage.css").toExternalForm());
            stage.setTitle("Login Page");
            stage.setScene(scene);
            stage.setResizable(false);
            stage.show();
        }
    }

    public void taskManager(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("taskManager.fxml"));
        Parent root = loader.load();
        TaskManager taskManagerController = loader.getController();
        stage = (Stage) ((MenuItem) event.getSource()).getParentPopup().getOwnerWindow();
        taskManagerController.setStage(stage);
        if (currentUsername != null) {
            taskManagerController.setCurrentUser(currentUsername);
        }
        scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/com/example/chronopanthers/taskManager.css").toExternalForm());
        stage.setTitle("Task Manager");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }

    public void aiAgent(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("aiAgent.fxml"));
        Parent root = loader.load();
        AIAgentController aiController = loader.getController();
        stage = (Stage) ((MenuItem) event.getSource()).getParentPopup().getOwnerWindow();
        aiController.setStage(stage);
        if (currentUsername != null) {
            aiController.setCurrentUser(currentUsername);
        }
        scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/com/example/chronopanthers/aiAgent.css").toExternalForm());
        stage.setTitle("AI Study Assistant");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }

    public void productivity(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("productivity.fxml"));
        Parent root = loader.load();
        Productivity productivity = loader.getController();
        stage = (Stage) ((MenuItem) event.getSource()).getParentPopup().getOwnerWindow();
        productivity.setStage(stage);
        if (currentUsername != null) {
            productivity.setCurrentUsername(currentUsername);
        }
        scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/com/example/chronopanthers/productivity.css").toExternalForm());
        stage.setTitle("Productivity Tracker");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
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