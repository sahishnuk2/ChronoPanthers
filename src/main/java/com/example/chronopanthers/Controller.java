package com.example.chronopanthers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import static com.example.chronopanthers.SupabaseConnection.*;

public class Controller implements Initializable {
    @FXML
    private Spinner<Integer> workDurationInput, breakDurationInput;
    @FXML
    private Button playButton;
    @FXML
    private Label timerDisplay, timerState, workSessionsDisplay, breakSessionsDisplay;
    @FXML
    private Circle progressRing;
    @FXML
    private VBox timeBox;
    @FXML
    private Label titleLabel;
    @FXML
    private NavigationController navigationBarController;

    private String currentUsername;
    private int workSessions = 0;
    private int breakSessions = 0;
    private boolean isControllerActive = true;
    private Stage stage;

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
            Platform.runLater(() -> {
                if (isControllerActive) {
                    updateDisplay(manager.timeLeft);
                    updateProgressRing(manager);
                }
            });
        });

        // Update mode UI on session switch
        manager.setOnModeSwitch(() -> {
            Platform.runLater(() -> {
                if (isControllerActive) {
                    updateModeUI(manager, true);
                }

                // Session counting logic (always runs, even when controller is inactive)
                if (manager.isWorkTime) {
                    // Just completed break, now work time!
                    breakSessions++;
                    if (isControllerActive) {
                        breakSessionsDisplay.setText(String.valueOf(breakSessions));
                    }
                    if (currentUsername != null) {
                        updateBreakSession(currentUsername);
                        logBreakSession(currentUsername, manager.breakTime / 60);
                    }
                } else {
                    // Just complete work, now break time!
                    workSessions++;
                    if (isControllerActive) {
                        workSessionsDisplay.setText(String.valueOf(workSessions));
                    }
                    if (currentUsername != null) {
                        updateWorkSession(currentUsername);
                        logWorkSession(currentUsername, manager.workTime / 60);
                    }
                }
            });
        });

        // Initial UI update
        updateDisplay(manager.timeLeft);
        updateProgressRing(manager);
        updateModeUI(manager, false);
    }

    private void updateModeUI(TimerManager manager, boolean playSound) {
        if (manager.isWorkTime) {
            timerState.setText("Work Time");
            timerState.setTextFill(Color.web("#3b82f6"));
            timeBox.setBackground(new Background(new BackgroundFill(Color.web("#f0f9ff"), new CornerRadii(16), Insets.EMPTY)));
            progressRing.setStroke(Color.web("#3b82f6"));
        } else {
            timerState.setText("Break Time");
            timerState.setTextFill(Color.web("#10b981"));
            timeBox.setBackground(new Background(new BackgroundFill(Color.web("#f0fdf4"), new CornerRadii(16), Insets.EMPTY)));
            progressRing.setStroke(Color.web("#10b981"));
        }

        if (playSound) {
            java.awt.Toolkit.getDefaultToolkit().beep(); // play sound
        }
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
        updateModeUI(TimerManager.getInstance(), false);
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
        //System.out.println("Controller: Setting username to: " + username);

        if (titleLabel != null) {
            titleLabel.setText("Let's Pomodoro, " + username);
        }

        // IMPORTANT: Always set the username in the navigation controller
        if (navigationBarController != null) {
            navigationBarController.setCurrentUser(username);
        }

        loadSessionCounts();
    }

    // Make sure this method is called after FXML injection
    public void initializeNavigation() {
        if (navigationBarController != null && currentUsername != null) {
            navigationBarController.setCurrentUser(currentUsername);
        }
    }

    // Call this when the controller becomes active (when user navigates to timer page)
    public void onControllerActivated() {
        isControllerActive = true;
        // Force UI update to sync with current timer state
        Platform.runLater(() -> {
            TimerManager manager = TimerManager.getInstance();
            updateDisplay(manager.timeLeft);
            updateProgressRing(manager);
            updateModeUI(manager, false); // This causes an extra beep sound when switching to timer

            // Update session displays with current counts
            workSessionsDisplay.setText(String.valueOf(workSessions));
            breakSessionsDisplay.setText(String.valueOf(breakSessions));

            // Update play button state
            if (manager.isRunning()) {
                playButton.setStyle("-fx-background-color: #10b981;");
            } else {
                playButton.setStyle("-fx-background-color: #3b82f6;");
            }
        });
    }

    // Call this when the controller becomes inactive (when user navigates away)
    public void onControllerDeactivated() {
        isControllerActive = false;
    }

    private void loadSessionCounts() {
        if (currentUsername != null) {
            int[] counts = SupabaseConnection.getSessionCounts(currentUsername);
            workSessions = counts[0];
            breakSessions = counts[1];
            workSessionsDisplay.setText(String.valueOf(workSessions));
            breakSessionsDisplay.setText(String.valueOf(breakSessions));
        }
    }

    public void updateDisplay(int timeLeft) {
        if (timerDisplay != null) {
            int minutes = timeLeft / 60;
            int seconds = timeLeft % 60;
            timerDisplay.setText(String.format("%02d:%02d", minutes, seconds));
        }
    }

    public void updateProgressRing(TimerManager manager) {
        if (progressRing != null) {
            double progress = (double) (manager.isWorkTime ? manager.workTime - manager.timeLeft : manager.breakTime - manager.timeLeft)
                    / (manager.isWorkTime ? manager.workTime : manager.breakTime);

            double circumference = 2 * Math.PI * progressRing.getRadius();
            double visibleLength = progress * circumference;

            progressRing.getStrokeDashArray().clear();
            progressRing.getStrokeDashArray().addAll(visibleLength, circumference);
            progressRing.setRotate(90);
        }
    }

    public void setStage(Stage stage) {
        this.stage = stage;

        this.stage.setOnCloseRequest(event -> {
            event.consume(); // prevent window from closing
            handleLogoutRequest(); // log out confirmation pop up
        });
    }

    private void handleLogoutRequest() {
        onControllerDeactivated(); // Mark as inactive

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