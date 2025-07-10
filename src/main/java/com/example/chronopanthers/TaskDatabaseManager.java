package com.example.chronopanthers;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class TaskDatabaseManager {

    // Get database connection
    private static Connection getConnection() throws SQLException {
        try {
            Class.forName("org.postgresql.Driver");
            return DriverManager.getConnection(DatabaseConfig.getJdbcUrl());
        } catch (ClassNotFoundException e) {
            throw new SQLException("PostgreSQL driver not found", e);
        }
    }

    // Add a new task to database
    public static boolean addTask(String username, Task task) {
        String sql = "INSERT INTO tasks (username, task_name, task_type, priority, is_completed, due_date) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            pstmt.setString(2, task.getTaskName());
            pstmt.setString(3, task.getTaskType());
            pstmt.setString(4, task.getPriority().toString());
            pstmt.setBoolean(5, task.getIsCompleted());

            // Handle due date - only set if it's a deadline task
            if (task.getDeadline() != null) {
                pstmt.setDate(6, Date.valueOf(task.getDeadline()));
            } else {
                pstmt.setNull(6, Types.DATE);
            }

            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("Task added successfully: " + task.getTaskName());
                return true;
            }

        } catch (SQLException e) {
            System.err.println("Error adding task: " + e.getMessage());
        }

        return false;
    }

    public static boolean taskExists(String username, String taskName) {
        //String sql = "SELECT 1 FROM tasks WHERE username = ? AND task_name = ?";
        String sql = "SELECT 1 FROM tasks WHERE username = ? AND LOWER(task_name) = LOWER(?) LIMIT 1";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            pstmt.setString(2, taskName);

            ResultSet rs = pstmt.executeQuery();
            return rs.next(); // true if a row exists

        } catch (SQLException e) {
            System.err.println("Error checking task existence: " + e.getMessage());
            return false;
        }
    }

    // Get all tasks for a specific user
    public static List<Task> getUserTasks(String username) {
        List<Task> tasks = new ArrayList<>();
        String sql = "SELECT task_name, task_type, priority, is_completed, due_date FROM tasks WHERE username = ? ORDER BY created_at DESC";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                String taskName = rs.getString("task_name");
                String taskType = rs.getString("task_type");
                String priorityStr = rs.getString("priority");
                boolean isCompleted = rs.getBoolean("is_completed");
                Date dueDate = rs.getDate("due_date");

                // Convert priority string to enum
                Task.Priority priority = Task.Priority.valueOf(priorityStr);

                // Create appropriate task type
                Task task;
                if ("Deadline".equals(taskType) && dueDate != null) {
                    LocalDate localDueDate = dueDate.toLocalDate();
                    task = new DeadlineTask(taskName, localDueDate, priority);
                } else {
                    task = new NormalTask(taskName, priority);
                }

                // Set completion status
                if (isCompleted) {
                    task.complete();
                }

                tasks.add(task);
            }

            System.out.println("Retrieved " + tasks.size() + " tasks for user: " + username);

        } catch (SQLException e) {
            System.err.println("Error retrieving tasks: " + e.getMessage());
        }

        return tasks;
    }

    // Update task completion status
    public static boolean updateTaskCompletion(String username, String taskName, boolean completed) {
        String sql = "UPDATE tasks SET is_completed = ? WHERE username = ? AND task_name = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setBoolean(1, completed);
            pstmt.setString(2, username);
            pstmt.setString(3, taskName);

            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("Task completion updated: " + taskName + " -> " + completed);
                return true;
            } else {
                System.out.println("No task found to update: " + taskName);
            }

        } catch (SQLException e) {
            System.err.println("Error updating task completion: " + e.getMessage());
        }

        return false;
    }

    // Delete a task
    public static boolean deleteTask(String username, String taskName) {
        String sql = "DELETE FROM tasks WHERE username = ? AND task_name = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            pstmt.setString(2, taskName);

            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("Task deleted successfully: " + taskName);
                return true;
            } else {
                System.out.println("No task found to delete: " + taskName);
            }

        } catch (SQLException e) {
            System.err.println("Error deleting task: " + e.getMessage());
        }

        return false;
    }

    // Get task count for a user
    public static int getUserTaskCount(String username) {
        String sql = "SELECT COUNT(*) as count FROM tasks WHERE username = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("count");
            }

        } catch (SQLException e) {
            System.err.println("Error getting task count: " + e.getMessage());
        }

        return 0;
    }

    // Get completed task count for a user
    public static int getUserCompletedTaskCount(String username) {
        String sql = "SELECT COUNT(*) as count FROM tasks WHERE username = ? AND is_completed = true";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("count");
            }

        } catch (SQLException e) {
            System.err.println("Error getting completed task count: " + e.getMessage());
        }

        return 0;
    }

    // Test database connection
    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            System.err.println("Database connection test failed: " + e.getMessage());
            return false;
        }
    }

    // Get overdue tasks for a user
    public static List<Task> getOverdueTasks(String username) {
        List<Task> overdueTasks = new ArrayList<>();
        String sql = "SELECT task_name, task_type, priority, is_completed, due_date FROM tasks " +
                "WHERE username = ? AND task_type = 'Deadline' AND due_date < CURRENT_DATE AND is_completed = false " +
                "ORDER BY due_date ASC";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                String taskName = rs.getString("task_name");
                String priorityStr = rs.getString("priority");
                Date dueDate = rs.getDate("due_date");

                Task.Priority priority = Task.Priority.valueOf(priorityStr);
                LocalDate localDueDate = dueDate.toLocalDate();

                Task task = new DeadlineTask(taskName, localDueDate, priority);
                overdueTasks.add(task);
            }

        } catch (SQLException e) {
            System.err.println("Error getting overdue tasks: " + e.getMessage());
        }

        return overdueTasks;
    }
}