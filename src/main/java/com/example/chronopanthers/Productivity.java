package com.example.chronopanthers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.Month;
import java.time.Year;
import java.time.format.TextStyle;
import java.util.*;

public class Productivity implements Initializable {
    @FXML
    private BarChart<String, Integer> taskChart, workSessionsChart, durationChart;
    @FXML
    private CategoryAxis taskxAxis, workSessxAxis, durationxAxis;
    @FXML
    private NumberAxis taskyAxis, workSessyAxis, durationyAxis;
    @FXML
    private ToggleGroup statsPer;
    @FXML
    private RadioButton weekly, monthly, yearly;
    @FXML
    private PieChart durationPieChart;
    @FXML
    private NavigationController navigationBarController;

    private String currentUsername;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialize axis labels
        taskxAxis.setLabel("Day");
        taskyAxis.setLabel("Task Completed");
        workSessxAxis.setLabel("Day");
        workSessyAxis.setLabel("WS Completed");
        durationxAxis.setLabel("Day");
        durationyAxis.setLabel("Duration/min");

        // Manually connect radio buttons to toggle group (fix for new FXML structure)
        weekly.setToggleGroup(statsPer);
        monthly.setToggleGroup(statsPer);
        yearly.setToggleGroup(statsPer);

        // Set default selection
        weekly.setSelected(true);

        // Initialize navigation
        if (navigationBarController != null) {
            navigationBarController.setCurrentUser(currentUsername);
        }

        // Add listener for toggle group changes
        statsPer.selectedToggleProperty().addListener((obs, oldValue, newValue) -> {
            if (weekly.isSelected()) {
                loadWeeklyChart();
            } else if (monthly.isSelected()){
                loadMonthlyChart();
            } else {
                loadYearlyChart();
            }
        });
    }

    public void setCurrentUsername(String username) {
        this.currentUsername = username;
        if (navigationBarController != null) {
            navigationBarController.setCurrentUser(username);
        }
        loadWeeklyChart();
    }

    public void loadWeeklyChart() {
        updateTaskBarByWeek(currentUsername);
        updateWorkChartByWeek(currentUsername);
        updateDurationChartByWeek(currentUsername);
        updateSessionPieChartByWeek(currentUsername);
    }

    public void loadMonthlyChart() {
        updateTaskBarByMonth(currentUsername);
        updateWorkChartByMonth(currentUsername);
        updateDurationChartByMonth(currentUsername);
        updateSessionPieChartByMonth(currentUsername);
    }

    public void loadYearlyChart() {
        updateTaskBarByYear(currentUsername);
        updateWorkChartByYear(currentUsername);
        updateDurationChartByYear(currentUsername);
        updateSessionPieChartByYear(currentUsername);
    }

    // Tasks
    public void updateTaskBarByWeek(String username) {
        Map<String, Integer> sessionsPerDay = TaskDatabaseManager.getTasksCompletedLast7Days(username);
        XYChart.Series<String, Integer> series = new XYChart.Series<>();
        series.setName("Last 7 Days");

        LocalDate today = LocalDate.now();
        List<String> days = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            String dbKey = date.toString();  // e.g., "2025-07-15"
            String dayOfWeek = date.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.getDefault()); // "Mon", etc.
            days.add(dayOfWeek);

            int count = sessionsPerDay.getOrDefault(dbKey, 0); // use 0 if not found
            series.getData().add(new XYChart.Data<>(dayOfWeek, count));
        }

        taskChart.getData().clear();
        taskChart.getData().add(series);
        resetTaskAxis("Days", days, 90);
    }

    public void updateTaskBarByMonth(String username) {
        Map<String, Integer> sessionsPerDay = TaskDatabaseManager.getTasksCompletedLast30Days(username);
        XYChart.Series<String, Integer> series = new XYChart.Series<>();
        series.setName("Last 30 Days");

        LocalDate today = LocalDate.now();
        List<String> days = new ArrayList<>();
        for (int i = 29; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            String dbKey = date.toString();  // e.g., "2025-07-15"
            String day = date.getDayOfMonth() + "/" + date.getMonthValue();
            days.add(day);

            int count = sessionsPerDay.getOrDefault(dbKey, 0); // use 0 if not found
            series.getData().add(new XYChart.Data<>(day, count));
        }

        taskChart.getData().clear();
        taskChart.getData().add(series);
        resetTaskAxis("Days", days, 90);
    }

    public void updateTaskBarByYear(String username) {
        Map<String, Integer> sessionsPerMonth = TaskDatabaseManager.getTasksCompletedThisYearByMonth(username);
        XYChart.Series<String, Integer> series = new XYChart.Series<>();
        series.setName("This Year");

        List<String> months = new ArrayList<>();
        for (int month = 1; month <= 12; month++) {
            String monthStr = String.format("%d-%02d", Year.now().getValue(), month);  // e.g., "2025-07"
            String label = Month.of(month).getDisplayName(TextStyle.SHORT, Locale.getDefault()); // e.g., "Jul"
            months.add(label);

            int count = sessionsPerMonth.getOrDefault(monthStr, 0);
            series.getData().add(new XYChart.Data<>(label, count));
        }

        taskChart.getData().clear(); // Fix: was updating wrong chart
        taskChart.getData().add(series);

        resetTaskAxis("Months", months, 90);
    }

    // Work Sessions
    public void updateWorkChartByWeek(String username) {
        Map<String, Integer> sessionsPerDay = SQliteConnection.getWorkSessionLast7Days(username);
        XYChart.Series<String, Integer> series = new XYChart.Series<>();
        series.setName("Last 7 Days");

        LocalDate today = LocalDate.now();
        List<String> dayLabels = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            String dbKey = date.toString();  // e.g., "2025-07-15"
            String dayOfWeek = date.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.getDefault()); // "Mon", etc.
            dayLabels.add(dayOfWeek);

            int count = sessionsPerDay.getOrDefault(dbKey, 0); // use 0 if not found
            series.getData().add(new XYChart.Data<>(dayOfWeek, count));
        }

        workSessionsChart.getData().clear();
        workSessionsChart.getData().add(series);
        resetWorkSessionAxis("Day", dayLabels, 90);
    }

    public void updateWorkChartByMonth(String username) {
        Map<String, Integer> sessionsPerDay = SQliteConnection.getWorkSessionLast30Days(username);
        XYChart.Series<String, Integer> series = new XYChart.Series<>();
        series.setName("Last 30 Days");

        LocalDate today = LocalDate.now();
        List<String> days = new ArrayList<>();
        for (int i = 29; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            String dbKey = date.toString();  // e.g., "2025-07-15"
            String day = date.getDayOfMonth() + "/" + date.getMonthValue();
            days.add(day);

            int count = sessionsPerDay.getOrDefault(dbKey, 0); // use 0 if not found
            series.getData().add(new XYChart.Data<>(day, count));
        }

        workSessionsChart.getData().clear();
        workSessionsChart.getData().add(series);
        resetWorkSessionAxis("Days", days, 90);
    }

    public void updateWorkChartByYear(String username) {
        Map<String, Integer> sessionsPerMonth = SQliteConnection.getWorkSessionThisYearByMonth(username);
        XYChart.Series<String, Integer> series = new XYChart.Series<>();
        series.setName("This Year");

        List<String> months = new ArrayList<>();
        for (int month = 1; month <= 12; month++) {
            String monthStr = String.format("%d-%02d", Year.now().getValue(), month);  // e.g., "2025-07"
            String label = Month.of(month).getDisplayName(TextStyle.SHORT, Locale.getDefault()); // e.g., "Jul"
            months.add(label);

            int count = sessionsPerMonth.getOrDefault(monthStr, 0);
            series.getData().add(new XYChart.Data<>(label, count));
        }

        workSessionsChart.getData().clear();
        workSessionsChart.getData().add(series);

        resetWorkSessionAxis("Months", months, 90);
    }

    // Duration
    public void updateDurationChartByWeek(String username) {
        Map<String, Integer> sessionsPerDay = SQliteConnection.getDurationLast7Days(username);
        XYChart.Series<String, Integer> series = new XYChart.Series<>();
        series.setName("Last 7 Days");

        LocalDate today = LocalDate.now();
        List<String> dayLabels = new ArrayList<>();

        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            String dbKey = date.toString();  // e.g., "2025-07-15"
            String dayOfWeek = date.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.getDefault()); // "Mon", etc.
            dayLabels.add(dayOfWeek);

            int count = sessionsPerDay.getOrDefault(dbKey, 0); // use 0 if not found
            series.getData().add(new XYChart.Data<>(dayOfWeek, count));
        }

        durationChart.getData().clear();
        durationChart.getData().add(series);

        resetDurationAxis("Day", dayLabels, 90);
    }

    public void updateDurationChartByMonth(String username) {
        Map<String, Integer> sessionsPerDay = SQliteConnection.getDurationLast30Days(username);
        XYChart.Series<String, Integer> series = new XYChart.Series<>();
        series.setName("Last 30 Days");

        LocalDate today = LocalDate.now();
        List<String> days = new ArrayList<>();
        for (int i = 29; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            String dbKey = date.toString();  // e.g., "2025-07-15"
            String day = date.getDayOfMonth() + "/" + date.getMonthValue();
            days.add(day);

            int count = sessionsPerDay.getOrDefault(dbKey, 0); // use 0 if not found
            series.getData().add(new XYChart.Data<>(day, count));
        }

        durationChart.getData().clear();
        durationChart.getData().add(series);

        resetDurationAxis("Days", days, 90);
    }

    public void updateDurationChartByYear(String username) {
        Map<String, Integer> sessionsPerMonth = SQliteConnection.getDurationThisYearByMonth(username);
        XYChart.Series<String, Integer> series = new XYChart.Series<>();
        series.setName("This Year");

        List<String> months = new ArrayList<>();
        for (int month = 1; month <= 12; month++) {
            String monthStr = String.format("%d-%02d", Year.now().getValue(), month);  // e.g., "2025-07"
            String label = Month.of(month).getDisplayName(TextStyle.SHORT, Locale.getDefault()); // e.g., "Jul"
            months.add(label);

            int count = sessionsPerMonth.getOrDefault(monthStr, 0);
            series.getData().add(new XYChart.Data<>(label, count));
        }

        durationChart.getData().clear();
        durationChart.getData().add(series);

        resetDurationAxis("Months", months, 90);
    }

    // Duration Pie Chart
    public void updateSessionPieChartByWeek(String username) {
        Map<String, Integer> durations = SQliteConnection.getTotalDurationsByType7Days(username);
        int workDuration = durations.getOrDefault("work", 0);
        int breakDuration = durations.getOrDefault("break", 0);

        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList(
                new PieChart.Data("Work", workDuration),
                new PieChart.Data("Break", breakDuration)
        );

        durationPieChart.setData(pieChartData);
        durationPieChart.setTitle("Last 7 Days");
    }

    public void updateSessionPieChartByMonth(String username) {
        Map<String, Integer> durations = SQliteConnection.getTotalDurationsByType30Days(username);
        int workDuration = durations.getOrDefault("work", 0);
        int breakDuration = durations.getOrDefault("break", 0);

        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList(
                new PieChart.Data("Work", workDuration),
                new PieChart.Data("Break", breakDuration)
        );

        durationPieChart.setData(pieChartData);
        durationPieChart.setTitle("Last 30 Days");
    }

    public void updateSessionPieChartByYear(String username) {
        Map<String, Integer> durations = SQliteConnection.getTotalDurationsByTypeYear(username);
        int workDuration = durations.getOrDefault("work", 0);
        int breakDuration = durations.getOrDefault("break", 0);

        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList(
                new PieChart.Data("Work", workDuration),
                new PieChart.Data("Break", breakDuration)
        );

        durationPieChart.setData(pieChartData);
        durationPieChart.setTitle("This Year");
    }

    private void resetDurationAxis(String label, List<String> categories, int rotation) {
        durationxAxis.setLabel(label);
        durationxAxis.getCategories().clear();
        durationxAxis.setTickLabelRotation(rotation);
        durationxAxis.setCategories(FXCollections.observableArrayList(categories));
        durationChart.layout(); // force redraw
    }

    private void resetWorkSessionAxis(String label, List<String> categories, int rotation) {
        workSessxAxis.setLabel(label);
        workSessxAxis.getCategories().clear();
        workSessxAxis.setTickLabelRotation(rotation);
        workSessxAxis.setCategories(FXCollections.observableArrayList(categories));
        workSessionsChart.layout(); // force redraw
    }

    private void resetTaskAxis(String label, List<String> categories, int rotation) {
        taskxAxis.setLabel(label);
        taskxAxis.getCategories().clear();
        taskxAxis.setTickLabelRotation(rotation);
        taskxAxis.setCategories(FXCollections.observableArrayList(categories));
        taskChart.layout(); // force redraw
    }

    // Remove old navigation methods since they're now handled by NavigationController
    // Keep only these if you need them for backward compatibility:
    private Stage stage;
    private Scene scene;

    public void Timer(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("timer.fxml"));
        Parent root = loader.load();
        Controller controller = loader.getController();
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
        FXMLLoader loader = new FXMLLoader(getClass().getResource("taskManager.fxml"));
        Parent root = loader.load();
        TaskManager controller = loader.getController();
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
        FXMLLoader loader = new FXMLLoader(getClass().getResource("AIAgent.fxml"));
        Parent root = loader.load();
        AIAgentController controller = loader.getController();
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