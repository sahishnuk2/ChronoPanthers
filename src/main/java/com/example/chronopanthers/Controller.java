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

public class Controller implements Initializable {
    @FXML
    private ProgressBar progressBar;
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

    private double progress;
    private int workTime = 25 * 60;
    private int breakTime = 5 * 60;
    private int timeLeft = workTime;
    private boolean running = false;
    private boolean isWorkTime = true;
    private int workSessions = 0;
    private int breakSessions = 0;
    private Timeline timeline;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        //Timers
        //progressBar.setStyle("-fx-accent: #00FF00");
        //Spinners
        SpinnerValueFactory<Integer> valueFactoryWork =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 60);
        valueFactoryWork.setValue(25);
        workDurationInput.setValueFactory(valueFactoryWork);

        SpinnerValueFactory<Integer> valueFactoryBreak =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 30);
        valueFactoryBreak.setValue(5);
        breakDurationInput.setValueFactory(valueFactoryBreak);

        //progressRing.getStrokeDashArray().add(0.1);
        //progressRing.getStrokeDashArray().add(600.0);

        //Timeline
        timeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            timeLeft--;

            if (timeLeft < 0) {
                // Time's up - switch modes
                if (isWorkTime) {
                    // Work session completed
                    isWorkTime = false;
                    timeLeft = breakTime;
                    workSessions++;
                    workSessionsDisplay.setText(String.valueOf(workSessions));

                    // Show notification
                    System.out.println("Break time! Time to relax for a few minutes.");

                    // Update UI for break mode
                    timerState.setText("Break Time");
                    timerState.setTextFill(Color.web("#10b981"));
                    timeBox.setBackground(new Background(new BackgroundFill(Color.web("#f0fdf4"), new CornerRadii(16), Insets.EMPTY)));
                    progressRing.setStroke(Color.web("#10b981"));
                } else {
                    // Break session completed
                    isWorkTime = true;
                    timeLeft = workTime;
                    breakSessions++;
                    breakSessionsDisplay.setText(String.valueOf(breakSessions));

                    // Show notification
                    System.out.println("Back to work! Focus on your next task.");

                    // Update UI for work mode
                    timerState.setText("Work Time");
                    timerState.setTextFill(Color.web("#3b82f6"));
                    timeBox.setBackground(new Background(new BackgroundFill(Color.web("#f0f9ff"), new CornerRadii(16), Insets.EMPTY)));
                    progressRing.setStroke(Color.web("#3b82f6"));
                }

                // Play sound
                java.awt.Toolkit.getDefaultToolkit().beep();
            }
            updateDisplay();
            updateProgressRing();

        }));
        timeline.setCycleCount(Animation.INDEFINITE);
    }

    public void play() {
        if (!running) {
            running = true;
            timeline.play();
            playButton.setStyle("-fx-background-color: #10b981;");

        }
    }

    public void pause() {
        if (running) {
            timeline.pause();
            running = false;
            playButton.setStyle("-fx-background-color: #3b82f6;");
        }
    }

    public void reset() {
        pause();
        running = false;
        isWorkTime = true;
        timeLeft = workTime;
        updateDisplay();
        updateProgressRing();

        timerState.setText("Work Time");
        timerState.setTextFill(Color.web("#3b82f6"));
        //progressBar.setProgress(0.0);
        timeBox.setBackground(new Background(new BackgroundFill(Color.web("#f0f9ff"), new CornerRadii(16), Insets.EMPTY)));
        progressRing.setStroke(Color.web("#3b82f6"));
    }

    public void updateDisplay() {
        int minutes = timeLeft / 60;
        int seconds = timeLeft % 60;
        timerDisplay.setText(String.format("%02d:%02d", minutes, seconds));

    }

    public void updateProgressRing() {
        int totalTime = isWorkTime ? workTime : breakTime;
        progress = (double) (totalTime - timeLeft) / totalTime;

        double circumference = 2 * Math.PI * progressRing.getRadius();
        double visibleLength = progress * circumference;

        progressRing.getStrokeDashArray().clear();
        progressRing.getStrokeDashArray().addAll(visibleLength, circumference);

        //progressRing.getStrokeDashArray().setAll(circumference, circumference);
        //progressRing.setStrokeDashOffset(circumference * (1.0 - progress));

        progressRing.setRotate(90);

    }

    public void applySettings() {
        if (!running) {
            workTime = workDurationInput.getValue() * 60;
            breakTime = breakDurationInput.getValue() * 60;
            timeLeft = workTime;
            //timerDisplay.setText(Integer.toString(workTime) + ":00");
            updateDisplay();
            updateProgressRing();
        } else {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Warning");
            alert.setHeaderText("Hi boss, there seems to be small miscommunication"); // Optional: can set a custom header or leave it null
            alert.setContentText("Pause first");
            alert.showAndWait();
        }
    }

    private Stage stage;
    private Scene scene;


    public void logout(ActionEvent event) throws IOException {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Logout");
        alert.setHeaderText("You're about to logout!");
        alert.setContentText("Do you want to save before exiting?: ");

        if(alert.showAndWait().get() == ButtonType.OK){
            Parent root = FXMLLoader.load(getClass().getResource("loginPage.fxml"));
            stage = (Stage) ((MenuItem) event.getSource()).getParentPopup().getOwnerWindow();
            scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/com/example/chronopanthers/loginPage.css").toExternalForm());
            stage.setTitle("Login Page");
            stage.setScene(scene);
            stage.show();
        }
    }
}