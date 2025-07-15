package com.example.chronopanthers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.MenuItem;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.Month;
import java.time.Year;
import java.time.format.TextStyle;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

public class Productivity implements Initializable {
    @FXML
    private BarChart<String, Integer> taskChart, workSessionsChart, durationChart;
    @FXML
    private CategoryAxis taskxAxis, workSessxAxis, durationxAxis;
    @FXML
    private NumberAxis taskyAxis, workSessyAxis, durationyAxis;

    private String currentUsername;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
//         Initialise to weekly at first
        taskxAxis.setLabel("Day");
        taskyAxis.setLabel("Task Completed");
        workSessxAxis.setLabel("Day");
        workSessyAxis.setLabel("WS Completed");
        durationxAxis.setLabel("Day");
        durationyAxis.setLabel("Duration/min");
        loadWeeklyChart();
    }

    public void setCurrentUsername(String username) {
        this.currentUsername = username;
        loadWeeklyChart();
    }

    public void loadWeeklyChart() {
        updateTaskBarByWeek(currentUsername);
        updateWorkChartByWeek(currentUsername);
        updateDurationChartByWeek(currentUsername);
    }

    // Tasks
    public void updateTaskBarByWeek(String username) {
        Map<String, Integer> sessionsPerDay = TaskDatabaseManager.getTasksCompletedLast7Days(username);
        XYChart.Series<String, Integer> series = new XYChart.Series<>();
        series.setName("This Week");

        LocalDate today = LocalDate.now();
        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            String dbKey = date.toString();  // e.g., "2025-07-15"
            String dayOfWeek = date.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.getDefault()); // "Mon", etc.

            int count = sessionsPerDay.getOrDefault(dbKey, 0); // use 0 if not found
            series.getData().add(new XYChart.Data<>(dayOfWeek, count));
        }

        taskChart.getData().clear();
        taskChart.getData().add(series);
    }

    // Work Sessions
    public void updateWorkChartByWeek(String username) {
        Map<String, Integer> sessionsPerDay = SQliteConnection.getWorkSessionLast7Days(username);
        XYChart.Series<String, Integer> series = new XYChart.Series<>();
        series.setName("This Week");

        LocalDate today = LocalDate.now();
        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            String dbKey = date.toString();  // e.g., "2025-07-15"
            String dayOfWeek = date.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.getDefault()); // "Mon", etc.

            int count = sessionsPerDay.getOrDefault(dbKey, 0); // use 0 if not found
            series.getData().add(new XYChart.Data<>(dayOfWeek, count));
        }

        workSessionsChart.getData().clear();
        workSessionsChart.getData().add(series);
    }

    public void updateDurationChartByWeek(String username) {
        Map<String, Integer> sessionsPerDay = SQliteConnection.getDurationLast7Days(username);
        XYChart.Series<String, Integer> series = new XYChart.Series<>();
        series.setName("This Week");

        LocalDate today = LocalDate.now();
        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            String dbKey = date.toString();  // e.g., "2025-07-15"
            String dayOfWeek = date.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.getDefault()); // "Mon", etc.

            int count = sessionsPerDay.getOrDefault(dbKey, 0); // use 0 if not found
            series.getData().add(new XYChart.Data<>(dayOfWeek, count));
        }

        durationChart.getData().clear();
        durationChart.getData().add(series);
    }


    private Stage stage;
    private Scene scene;

    public void Timer(ActionEvent event) throws IOException {
        // Load Timer with current user context
        FXMLLoader loader = new FXMLLoader(getClass().getResource("timer.fxml"));
        Parent root = loader.load();

        // Get the TaskManager controller and set the current user
        Controller controller = loader.getController();
        //controller.setCurrentUser(currentUsername);
        if (currentUsername != null) {
            controller.setCurrentUser(currentUsername);
        }

        stage = (Stage) ((MenuItem) event.getSource()).getParentPopup().getOwnerWindow();
        scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/com/example/chronopanthers/timer.css").toExternalForm());
        stage.setTitle("Timer");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }

    public void TaskManager(ActionEvent event) throws IOException {
        // Load TaskManager with current user context
        FXMLLoader loader = new FXMLLoader(getClass().getResource("taskManager.fxml"));
        Parent root = loader.load();

        // Get the TaskManager controller and set the current user
        TaskManager controller = loader.getController();
        //controller.setCurrentUser(currentUsername);
        if (currentUsername != null) {
            controller.setCurrentUser(currentUsername);
        }

        stage = (Stage) ((MenuItem) event.getSource()).getParentPopup().getOwnerWindow();
        scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/com/example/chronopanthers/taskManager.css").toExternalForm());
        stage.setTitle("Task Manager");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }

    public void AIAgent(ActionEvent event) throws IOException {
        // Load AI Assist with current user context
        FXMLLoader loader = new FXMLLoader(getClass().getResource("AIAgent.fxml"));
        Parent root = loader.load();

        // Get the TaskManager controller and set the current user
        AIAgentController controller = loader.getController();
        //controller.setCurrentUser(currentUsername);
        if (currentUsername != null) {
            controller.setCurrentUser(currentUsername);
        }

        stage = (Stage) ((MenuItem) event.getSource()).getParentPopup().getOwnerWindow();
        scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/com/example/chronopanthers/AIAgent.css").toExternalForm());
        stage.setTitle("AI Study Assistant");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }

    public void logout(ActionEvent event) throws IOException {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Logout");
        alert.setHeaderText("You're about to logout!");
        alert.setContentText("Have you completed all your work?");

        if(alert.showAndWait().get() == ButtonType.OK){
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
}
