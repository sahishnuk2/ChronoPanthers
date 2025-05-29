package com.example.chronopanthers;
import java.sql.*;

public class SQliteConnection {
    public static Connection connector() {
        try {
            Class.forName("org.sqlite.JDBC");
            Connection conn = DriverManager.getConnection("jdbc:sqlite:PomodoroLoginDB.db");
            System.out.println("Connection successful!");
            return conn;
        } catch (Exception e) {
            System.out.println(e);
            return null;
        }
    }
}

