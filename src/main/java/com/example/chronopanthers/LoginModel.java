package com.example.chronopanthers;
import javafx.scene.control.Alert;

import java.sql.*;

public class LoginModel {
    Connection connection;

    public LoginModel() {
        connection = SQliteConnection.connector();
        if (connection == null) System.exit(1);
    }

    public boolean isDbConnected() {
        try {
            return !connection.isClosed();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean isLogin(String user, String pass) throws SQLException {
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        String query = "SELECT * FROM loginDetails WHERE username = ? AND password = ?";
        try {
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, user);
            preparedStatement.setString(2, pass);

            resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            return false;
        } finally {
            preparedStatement.close();
            resultSet.close();
        }
    }

    public boolean isSignUp(String user, String pass) throws SQLException {
        PreparedStatement selectStatement = null;
        ResultSet resultSet = null;
        PreparedStatement insertStatement=  null;
        String query = "SELECT * FROM loginDetails WHERE username = ?";
        try {
            selectStatement = connection.prepareStatement(query);
            selectStatement.setString(1, user);

            resultSet = selectStatement.executeQuery();
            if (resultSet.next()) {
                return false; // already signed up
            } else {
                query = "INSERT INTO loginDetails (username, password) VALUES (?, ?)";
                insertStatement = connection.prepareStatement(query);
                insertStatement.setString(1, user);
                insertStatement.setString(2, pass);
                int rowsInserted = insertStatement.executeUpdate();
                if (rowsInserted > 0) {
                    return true;
                } else {
                    return false;
                }
            }
        } catch (Exception e) {
            return false;
        } finally {
            if (selectStatement != null) selectStatement.close();
            if (insertStatement != null) insertStatement.close();
            if (resultSet != null) resultSet.close();
//            insertStatement.close();
//            resultSet.close();
        }

    }
}
