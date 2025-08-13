package com.example.chronopanthers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;

public class TaskDescription implements Initializable {
    @FXML
    private TextField taskName;
    @FXML
    private RadioButton normal, deadline;
    @FXML
    private DatePicker dueDate;
    @FXML
    private ToggleGroup taskType;
    @FXML
    private Button addTaskButton;
    @FXML
    private Label label;
    @FXML
    private ComboBox<Task.Priority> priorityComboBox;
    @FXML
    private Button cancel;

    private Task task;
    private boolean isEditing = false;
    private String originalTaskName;
    private String user;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        taskType.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
            if (deadline.isSelected()) {
                dueDate.setDisable(false);
            } else {
                dueDate.setDisable(true);
            }
        });

        priorityComboBox.getItems().addAll(Task.Priority.values());
    }

    public void setUser(String user) {
        this.user = user;
    }

    // Adds a new task or edits a task (delete and insert task)
    public void addTask() {
        if (taskName.getText().isBlank()) {
            label.setText("Empty Task Name!");
            return;
        }

        if (priorityComboBox.getValue() == null) {
            label.setText("Choose Priority!");
            return;
        }

        String taskname = taskName.getText();
        boolean isNormal = taskType.getSelectedToggle() == normal;
        Task.Priority priority = priorityComboBox.getValue();
        LocalDate date = dueDate.getValue();

        if (!isNormal && date == null) {
            label.setText("Due date not selected");
            return;
        }

        // Check for duplicate task
        if (!isEditing && TaskDatabaseManager.taskExists(user, taskname)) {
            label.setText("Task with this name already exists!");
            return;
        }

        if (isNormal) {
            task = new NormalTask(taskname, priority);
        } else {
            task = new DeadlineTask(taskname, date, priority);
        }
        ((Stage) normal.getScene().getWindow()).close();
    }

    public Task getTask() {
        return this.task;
    }

    public void cancel() {
        task = null;
        ((Stage) cancel.getScene().getWindow()).close();
    }

    // To be used only for editing
    public void setTask(Task task) {
        this.task = task;
        this.isEditing = true;
        this.originalTaskName = task.getTaskName();
        taskName.setText(originalTaskName);
        priorityComboBox.setValue(task.getPriority());

        if (task instanceof NormalTask) {
            normal.setSelected(true);
            dueDate.setDisable(true);
        } else {
            deadline.setSelected(true);
            dueDate.setDisable(false);
            dueDate.setValue(task.getDeadline());
        }

        addTaskButton.setText("Save");
    }


}
