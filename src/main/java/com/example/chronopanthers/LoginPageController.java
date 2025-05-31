package com.example.chronopanthers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class LoginPageController implements Initializable {
    @FXML
    private Button login;
    @FXML
    private Label isConnected;
    @FXML
    private TextField txtUsername, txtPassword;

    private Stage stage;
    private Scene scene;
    private Parent root;

    public LoginModel loginModel = new LoginModel();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        if (loginModel.isDbConnected()) {
            isConnected.setText("Connected to DB");
        } else {
            isConnected.setText("Not Connected to DB");
        }
    }

    public void login(ActionEvent event) throws IOException {
        try {
            if (loginModel.isLogin(txtUsername.getText(), txtPassword.getText())) {
                isConnected.setText("Correct");

                // Use FXMLLoader to get the controller
                FXMLLoader loader = new FXMLLoader(getClass().getResource("timer.fxml"));
                Parent root = loader.load();

                // Get the controller and set the current user
                Controller mainController = loader.getController();
                mainController.setCurrentUser(txtUsername.getText());

                stage = (Stage) ((Node)event.getSource()).getScene().getWindow();
                scene = new Scene(root);
                scene.getStylesheets().add(getClass().getResource("/com/example/chronopanthers/timer.css").toExternalForm());
                stage.setTitle("Timer");
                stage.setScene(scene);
                stage.show();
            } else {
                isConnected.setText("Username or password is incorrect");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }







    }
    public void signup(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("SignUpPage.fxml"));
        stage = (Stage) ((Node)event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/com/example/chronopanthers/signUpPage.css").toExternalForm());
        stage.setScene(scene);
        stage.show();
    }


}