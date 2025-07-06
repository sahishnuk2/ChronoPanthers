package com.example.chronopanthers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;

public class TaskManager implements Initializable {
    @FXML
    private TableView<Task> taskTable;
    @FXML
    private TableColumn<Task, String> type;
    @FXML
    private TableColumn<Task, String> name;
    @FXML
    private TableColumn<Task, LocalDate> dueDate;
    @FXML
    private TableColumn<Task, Boolean> completed;
    @FXML
    private TableColumn<Task, Boolean> overdue;
    @FXML
    private ComboBox<TaskComparator.SortMode> sortBox;
    @FXML
    private Label sorterLabel;
    @FXML
    private TableColumn<Task, String> priority;
    @FXML
    private Button completeTaskButton;
    @FXML
    private Button deleteTaskButton;
    @FXML
    private Button refreshButton;
    @FXML
    private Label usernameLabel;
    @FXML
    private Label taskStatsLabel;
    @FXML
    private TextField searchName;
    @FXML
    private ComboBox<String> typeFilter;
    @FXML
    private ComboBox<Task.Priority> priorityFilter;

    private ObservableList<Task> tasks = FXCollections.observableArrayList();
    private String currentUsername;
    private FilteredList<Task> filteredTasks;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        taskTable.setFixedCellSize(40);
        // Set up table columns
        type.setCellValueFactory(new PropertyValueFactory<Task, String>("taskType"));
        name.setCellValueFactory(new PropertyValueFactory<Task, String>("taskName"));
        dueDate.setCellValueFactory(new PropertyValueFactory<Task, LocalDate>("deadline"));
        completed.setCellValueFactory(new PropertyValueFactory<Task, Boolean>("isCompleted"));
        overdue.setCellValueFactory(new PropertyValueFactory<Task, Boolean>("isOverdue"));
        priority.setCellValueFactory(new PropertyValueFactory<Task, String>("priority"));

        // Tasks to be in this list for filtering
        filteredTasks = new FilteredList<>(tasks, p -> true);

        typeFilter.getItems().addAll("All", "Normal", "Deadline");
        typeFilter.setValue("All");

        priorityFilter.getItems().add(null);
        priorityFilter.getItems().addAll(Task.Priority.values());
        priorityFilter.setValue(null);

        sortBox.getItems().addAll(TaskComparator.SortMode.values());
        sortBox.setValue(TaskComparator.SortMode.NIL);


        searchName.textProperty().addListener((obs, oldVal, newVal) -> {
            updateFilter(filteredTasks);
        });

        typeFilter.valueProperty().addListener((obs, oldVal, newVal) -> {
            updateFilter(filteredTasks);
        });

        priorityFilter.valueProperty().addListener((obs, oldVal, newVal) -> {
            updateFilter(filteredTasks);
        });

        sortBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            sortTasks();
        });

        taskTable.setItems(filteredTasks);

        

        // Set default username (this should be set by the calling controller)
        currentUsername = "testuser"; // Default for testing
        updateLabels();

        // Test database connection
        if (TaskDatabaseManager.testConnection()) {
            System.out.println("✓ Task database connection successful");
        } else {
            sorterLabel.setText("Database connection failed");
        }



    }

    private void updateFilter(FilteredList<Task> filteredList) {
        String prefix = searchName.getText().toLowerCase();
        Task.Priority priority = priorityFilter.getValue(); // can be null
        String type = typeFilter.getValue();

        filteredList.setPredicate(task -> {
            boolean matchName = prefix == null || prefix.isEmpty() || task.getTaskName().toLowerCase().contains(prefix);
            boolean matchPriority = priority == null || task.getPriority().equals(priority);
            boolean matchType = type.equals("All") || task.getTaskType().equalsIgnoreCase(type);
            return matchName && matchType && matchPriority;
        });
    }

//    private boolean filterByPriority (Task task, Task.Priority priority) {
//        return true;
//    }
//
//    private boolean filterByTaskType (Task task, String TaskType) {
//        return task.getTaskType().equalsIgnoreCase(TaskType);
//    }
//
//    private boolean filterByName(Task task, String prefix) {
//        if (prefix == null || prefix.isEmpty()) return true; // no need to filter
//
//        prefix  = prefix.toLowerCase();
//        return task.getTaskName().toLowerCase().contains(prefix);
//        // task.getTaskType().toLowerCase().contains(prefix);
//        // task.getPriority().toLowerCase().contains(prefix);
//    }

    // Method to set the current user (call this from your main controller)
    public void setCurrentUser(String username) {
        this.currentUsername = username;
        updateLabels();
        loadUserTasks();
    }

    private void updateLabels() {
        if (currentUsername != null) {
            if (usernameLabel != null) {
                usernameLabel.setText("Tasks for: " + currentUsername);
            }
            updateTaskStats();
        }
    }

    private void updateTaskStats() {
        if (taskStatsLabel != null && currentUsername != null) {
            int totalTasks = TaskDatabaseManager.getUserTaskCount(currentUsername);
            int completedTasks = TaskDatabaseManager.getUserCompletedTaskCount(currentUsername);
            int pendingTasks = totalTasks - completedTasks;

            taskStatsLabel.setText(String.format("Total: %d | Completed: %d | Pending: %d",
                    totalTasks, completedTasks, pendingTasks));
        }
    }

    // Load tasks from database
    public void loadUserTasks() {
        if (currentUsername == null) {
            sorterLabel.setText("No user logged in");
            return;
        }

        try {
            List<Task> userTasks = TaskDatabaseManager.getUserTasks(currentUsername);
            tasks.clear();
            tasks.addAll(userTasks);

            sorterLabel.setText("Loaded " + userTasks.size() + " tasks");
            updateTaskStats();

            // Check for overdue tasks
            List<Task> overdueTasks = TaskDatabaseManager.getOverdueTasks(currentUsername);
            if (!overdueTasks.isEmpty()) {
                sorterLabel.setText(sorterLabel.getText() + " (" + overdueTasks.size() + " overdue!)");
            }

        } catch (Exception e) {
            sorterLabel.setText("Failed to load tasks");
            System.err.println("Error loading tasks: " + e.getMessage());
        }
    }

    // Refresh tasks from database
    @FXML
    public void refreshTasks() {
        loadUserTasks();
        sorterLabel.setText("Tasks refreshed");
    }

    @FXML
    public void addTask() throws IOException {
        if (currentUsername == null) {
            sorterLabel.setText("Please log in first");
            return;
        }

        FXMLLoader fxmlLoader = new FXMLLoader(ChronoPanthers.class.getResource("addingTaskPage.fxml"));
        DialogPane dialogPane = fxmlLoader.load();

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setDialogPane(dialogPane);
        dialog.setTitle("Add new task");
        dialogPane.getStylesheets().add(getClass().getResource("/com/example/chronopanthers/addingTaskPage.css").toExternalForm());

        dialog.showAndWait();

        TaskDescription controller = fxmlLoader.getController();
        Task task = controller.getTask();

        if (task != null) {
            // Save to database
            boolean success = TaskDatabaseManager.addTask(currentUsername, task);

            if (success) {
                // Reload tasks from database to get the latest data
                loadUserTasks();
                sorterLabel.setText("Task added successfully!");
            } else {
                sorterLabel.setText("Failed to add task to database");
            }
        }
    }

    @FXML
    public void sortTasks() {
        TaskComparator.SortMode selectedMode = sortBox.getValue();
        if (selectedMode != null) {
            sorterLabel.setText("");
            FXCollections.sort(tasks, new TaskComparator(selectedMode));
        } else {
            sorterLabel.setText("Choose a sorter!");
        }
    }

    @FXML
    public void completeTask() {
        Task selectedTask = taskTable.getSelectionModel().getSelectedItem();
        if (selectedTask == null) {
            sorterLabel.setText("Please select a task to complete");
            return;
        }

        if (selectedTask.getIsCompleted()) {
            sorterLabel.setText("Task is already completed");
            return;
        }

        // Update in database
        boolean success = TaskDatabaseManager.updateTaskCompletion(
                currentUsername,
                selectedTask.getTaskName(),
                true
        );

        if (success) {
            // Update local object
            selectedTask.complete();
            taskTable.refresh();
            updateTaskStats();

            // Re-sort to move completed tasks to bottom
            FXCollections.sort(tasks, new TaskComparator(TaskComparator.SortMode.DEADLINE_FIRST));

            sorterLabel.setText("Task marked as completed!");
        } else {
            sorterLabel.setText("Failed to update task in database");
        }
    }

    // New method to delete selected task
    @FXML
    public void deleteTask() {
        Task selectedTask = taskTable.getSelectionModel().getSelectedItem();
        if (selectedTask == null) {
            sorterLabel.setText("Please select a task to delete");
            return;
        }

        // Confirm deletion
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Delete Task");
        confirmation.setHeaderText("Are you sure you want to delete this task?");
        confirmation.setContentText("Task: " + selectedTask.getTaskName());

        if (confirmation.showAndWait().get() == ButtonType.OK) {
            // Delete from database
            boolean success = TaskDatabaseManager.deleteTask(currentUsername, selectedTask.getTaskName());

            if (success) {
                // Remove from local list
                tasks.remove(selectedTask);
                updateTaskStats();
                sorterLabel.setText("Task deleted successfully!");
            } else {
                sorterLabel.setText("Failed to delete task from database");
            }
        }
    }

    // Show only overdue tasks
    @FXML
    public void showOverdueTasks() {
        if (currentUsername == null) {
            sorterLabel.setText("No user logged in");
            return;
        }

        List<Task> overdueTasks = TaskDatabaseManager.getOverdueTasks(currentUsername);
        tasks.clear();
        tasks.addAll(overdueTasks);

        sorterLabel.setText("Showing " + overdueTasks.size() + " overdue tasks");
    }

    // Show all tasks (reset filter)
    @FXML
    public void showAllTasks() {
        loadUserTasks();
    }

    private Stage stage;
    private Scene scene;
    private Parent root;

    public void timer(ActionEvent event) throws IOException {
        // Load the timer page with the current user
        FXMLLoader loader = new FXMLLoader(getClass().getResource("timer.fxml"));
        Parent root = loader.load();

        // Pass the current user to the timer controller
        Controller timerController = loader.getController();
        if (currentUsername != null) {
            timerController.setCurrentUser(currentUsername);
        }

        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/com/example/chronopanthers/timer.css").toExternalForm());
        stage.setTitle("Timer");
        stage.setScene(scene);
        stage.show();
    }

    @FXML
    public void aiAgent(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("aiAgent.fxml"));
        Parent root = loader.load();

        AIAgentController aiController = loader.getController();
        if (currentUsername != null) {
            aiController.setCurrentUser(currentUsername);
        }

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/com/example/chronopanthers/aiAgent.css").toExternalForm());
        stage.setTitle("AI Study Assistant");
        stage.setScene(scene);
        stage.show();
    }
}