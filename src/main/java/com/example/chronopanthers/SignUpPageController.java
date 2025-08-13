package com.example.chronopanthers;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class SignUpPageController implements Initializable {

    @FXML
    private Button signUpButton;
    @FXML
    private Label isConnected;
    @FXML
    private TextField txtUsername, txtPassword;
    @FXML
    private ImageView imageView;

    private Stage stage;
    private Scene scene;
    private Parent root;

    public LoginModel loginModel = new LoginModel();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Image myImage = new Image(getClass().getResourceAsStream("Images/Panther.jpeg"));
        imageView.setImage(myImage);

        Platform.runLater(() -> {
            signUpButton.requestFocus();
            Stage stage = (Stage) signUpButton.getScene().getWindow();
            stage.setOnCloseRequest(null); // reset to default close behavior
        });

        if (loginModel.isDbConnected()) {
            isConnected.setText("");
            System.out.println("Connected to DB");
        } else {
            isConnected.setText("Not Connected to DB");
        }
    }

    // If signup successful, go back to login page after saving user info
    public void signUp(ActionEvent event) throws IOException {
        try {
            if (txtUsername.getText().isBlank() ) {
                isConnected.setText("Username Empty!");
            } else if (txtPassword.getText().isBlank()) {
                isConnected.setText("Password Empty!");
            } else if (txtPassword.getText().length() < 8) {
                isConnected.setText("Password Min Length: 8");
            } else if (!loginModel.isSignUp(txtUsername.getText(), txtPassword.getText())) {
                isConnected.setText("Username already used!");
            } else {
                isConnected.setText("");
                Alert confirmation = new Alert(Alert.AlertType.INFORMATION);
                confirmation.setTitle("Sign up successful");
                confirmation.setHeaderText("Username and password accepted");
                confirmation.setContentText("Proceed to log in!");
                confirmation.showAndWait();

                Parent root = FXMLLoader.load(getClass().getResource("loginPage.fxml"));
                stage = (Stage) ((Node)event.getSource()).getScene().getWindow();
                scene = new Scene(root);
                scene.getStylesheets().add(getClass().getResource("/com/example/chronopanthers/loginPage.css").toExternalForm());
                stage.setTitle("Login Page");
                stage.setScene(scene);
                stage.setResizable(false);
                stage.show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Go back to login page
    public void back(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("loginPage.fxml"));
        stage = (Stage) ((Node)event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/com/example/chronopanthers/loginPage.css").toExternalForm());
        stage.setTitle("Login Page");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }
}