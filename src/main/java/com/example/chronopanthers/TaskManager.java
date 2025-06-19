package com.example.chronopanthers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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

import javax.imageio.IIOException;
import java.io.IOException;
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

    public void addTask() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(ChronoPanthers.class.getResource("addingTaskPage.fxml"));
        DialogPane dialogPane = fxmlLoader.load();

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setDialogPane(dialogPane);
        dialog.setTitle("Add new task");

        dialog.showAndWait();

        TaskDescription controller = fxmlLoader.getController();
        Task task = controller.getTask();

        if (task == null) {

        } else {

            tasks.add(task);
        }
    }

    private Stage stage;
    private Scene scene;
    private Parent root;

    public void timer(ActionEvent event) throws IOException {
//        Parent root = FXMLLoader.load(getClass().getResource("timer.fxml"));
//        stage = (Stage) ((MenuItem) event.getSource()).getParentPopup().getOwnerWindow();
//        scene = new Scene(root);
//        scene.getStylesheets().add(getClass().getResource("/com/example/chronopanthers/loginPage.css").toExternalForm());
//        stage.setTitle("Login Page");
//        stage.setScene(scene);
//        stage.show();

        Parent root = FXMLLoader.load(getClass().getResource("timer.fxml"));
        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/com/example/chronopanthers/signUpPage.css").toExternalForm());
        stage.setTitle("Sign Up Page");
        stage.setScene(scene);
        stage.show();
    }
}
