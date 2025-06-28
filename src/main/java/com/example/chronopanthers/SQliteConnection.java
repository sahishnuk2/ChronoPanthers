package com.example.chronopanthers;
import java.sql.*;

public class SQliteConnection {
    public static Connection connector() {
        try {
            Class.forName("org.postgresql.Driver");

            Connection conn = DriverManager.getConnection(DatabaseConfig.getJdbcUrl());

            System.out.println("Connection to Supabase successful!");
            return conn;

        } catch (ClassNotFoundException e) {
            System.err.println("PostgreSQL JDBC Driver not found: " + e.getMessage());
            return null;
        } catch (SQLException e) {
            System.err.println("Database connection failed: " + e.getMessage());
            System.err.println("JDBC URL: " + DatabaseConfig.getJdbcUrl());
            return null;
        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
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
        int[] counts = {0, 0};

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

    // Additional method to test connection
    public static boolean testConnection() {
        try (Connection conn = connector()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            System.err.println("Connection test failed: " + e.getMessage());
            return false;
        }
    }
}