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

    public static void updateWorkSession(String username) {
        String sql = "UPDATE loginDetails SET workSessions = workSessions + 1 WHERE username = ?";

        try (Connection conn = connector();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            if (conn != null) {
                pstmt.setString(1, username);
                int rowsAffected = pstmt.executeUpdate();
                System.out.println("Work session updated for " + username + ". Rows affected: " + rowsAffected);
            }

        } catch (SQLException e) {
            System.err.println("Error updating work session: " + e.getMessage());
        }
    }

    public static void updateBreakSession(String username) {
        String sql = "UPDATE loginDetails SET breakSessions = breakSessions + 1 WHERE username = ?";

        try (Connection conn = connector();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            if (conn != null) {
                pstmt.setString(1, username);
                int rowsAffected = pstmt.executeUpdate();
                System.out.println("Break session updated for " + username + ". Rows affected: " + rowsAffected);
            }

        } catch (SQLException e) {
            System.err.println("Error updating break session: " + e.getMessage());
        }
    }

    public static int[] getSessionCounts(String username) {
        String sql = "SELECT workSessions, breakSessions FROM loginDetails WHERE username = ?";
        int[] counts = {0, 0}; // [workSessions, breakSessions]

        try (Connection conn = connector();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            if (conn != null) {
                pstmt.setString(1, username);
                ResultSet rs = pstmt.executeQuery();

                if (rs.next()) {
                    counts[0] = rs.getInt("workSessions");
                    counts[1] = rs.getInt("breakSessions");
                    System.out.println("Retrieved sessions for " + username +
                            " - Work: " + counts[0] + ", Break: " + counts[1]);
                }
            }

        } catch (SQLException e) {
            System.err.println("Error getting session counts: " + e.getMessage());
        }

        return counts;
    }
}

