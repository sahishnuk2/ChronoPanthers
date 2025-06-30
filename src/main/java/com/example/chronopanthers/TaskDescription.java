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
    private Label username;
    @FXML
    private ComboBox<Task.Priority> priorityComboBox;
    @FXML
    private Button cancel;

    private Task task;



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

    public void addTask() {
        if (taskName.getText().isBlank()) {
            username.setText("Empty TaskName!");
            return;
        }

        if (priorityComboBox.getValue() == null) {
            username.setText("Choose Priority!");
            return;
        }

        String taskname = taskName.getText();
        boolean isNormal = taskType.getSelectedToggle() == normal;
        Task.Priority priority = priorityComboBox.getValue();
        LocalDate date = dueDate.getValue();

        if (!isNormal && date == null) {
            username.setText("Due date not selected");
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
}
