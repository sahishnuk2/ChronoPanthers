package com.example.chronopanthers;
import java.sql.*;
import java.util.LinkedHashMap;
import java.util.Map;

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

    public static void logWorkSession(String username, int duration) {
        String sql = "INSERT INTO sessionslog (username, session_type, duration) VALUES (?, ?, ?)";

        try (Connection conn = connector();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            pstmt.setString(2, "work");
            pstmt.setInt(3, duration);

            int rowsInserted = pstmt.executeUpdate();
            System.out.println("Logged work session for " + username + ". Rows inserted: " + rowsInserted);

        } catch (SQLException e) {
            System.err.println("Error logging work session: " + e.getMessage());
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

    public static void logBreakSession(String username, int duration) {
        String sql = "INSERT INTO sessionslog (username, session_type, duration) VALUES (?, ?, ?)";

        try (Connection conn = connector();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            pstmt.setString(2, "break");
            pstmt.setInt(3, duration);

            int rowsInserted = pstmt.executeUpdate();
            System.out.println("Logged break session for " + username + ". Rows inserted: " + rowsInserted);

        } catch (SQLException e) {
            System.err.println("Error logging break session: " + e.getMessage());
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

    public static Map<String, Integer> getWorkSessionLast7Days(String username) {
        String sql = " SELECT TO_CHAR(created_at, 'YYYY-MM-DD') AS day, COUNT(*) AS task_count FROM sessionslog WHERE session_type = 'work' " +
                     " AND username = ? AND created_at >= CURRENT_DATE - INTERVAL '6 days'" +
                     "GROUP BY day ORDER BY day";

        Map<String, Integer> result = new LinkedHashMap<>();

        try (Connection conn = connector();
             PreparedStatement stmt = conn.prepareStatement(sql)) {


            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                result.put(rs.getString("day"), rs.getInt("task_count"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return result;
    }

    public static Map<String, Integer> getDurationLast7Days(String username) {
        String sql = " SELECT TO_CHAR(created_at, 'YYYY-MM-DD') AS day, SUM(duration) AS total_duration FROM sessionslog WHERE session_type = 'work' " +
                " AND username = ? AND created_at >= CURRENT_DATE - INTERVAL '6 days'" +
                "GROUP BY day ORDER BY day";

        Map<String, Integer> result = new LinkedHashMap<>();

        try (Connection conn = connector();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                result.put(rs.getString("day"), rs.getInt("total_duration"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return result;
    }

    public static Map<String, Integer> getDurationLast30Days(String username) {
        String sql = """
        SELECT TO_CHAR(created_at, 'YYYY-MM-DD') AS day, SUM(duration) AS total_duration
        FROM sessionslog
        WHERE session_type = 'work'
          AND username = ?
          AND created_at >= CURRENT_DATE - INTERVAL '29 days'
        GROUP BY day
        ORDER BY day;
        """;

        Map<String, Integer> result = new LinkedHashMap<>();

        try (Connection conn = connector();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                result.put(rs.getString("day"), rs.getInt("total_duration"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return result;
    }

    public static Map<String, Integer> getDurationThisYearByMonth(String username) {
        String sql = """
        SELECT TO_CHAR(created_at, 'YYYY-MM') AS month, SUM(duration) AS total_duration
        FROM sessionslog
        WHERE session_type = 'work'
          AND username = ?
          AND EXTRACT(YEAR FROM created_at) = EXTRACT(YEAR FROM CURRENT_DATE)
        GROUP BY month
        ORDER BY month;
        """;

        Map<String, Integer> result = new LinkedHashMap<>();

        try (Connection conn = connector();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                result.put(rs.getString("month"), rs.getInt("total_duration"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return result;
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