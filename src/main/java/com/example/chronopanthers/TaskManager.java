package com.example.chronopanthers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.time.LocalDate;
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

    private final ObservableList<Task> tasks = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        type.setCellValueFactory(new PropertyValueFactory<Task, String>("taskType"));
        name.setCellValueFactory(new PropertyValueFactory<Task, String>("taskName"));
        dueDate.setCellValueFactory(new PropertyValueFactory<Task, LocalDate>("deadline"));
        completed.setCellValueFactory(new PropertyValueFactory<Task, Boolean>("isCompleted"));
        overdue.setCellValueFactory(new PropertyValueFactory<Task, Boolean>("isOverdue"));

        taskTable.setItems(tasks);
    }

    public void addTask(Task task) {
        tasks.add(task);
    }
}
