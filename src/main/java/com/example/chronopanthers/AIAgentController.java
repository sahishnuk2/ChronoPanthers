package com.example.chronopanthers;

import javafx.application.Platform;
import javafx.concurrent.Task;
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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

public class AIAgentController implements Initializable {
    @FXML
    private TextArea chatHistory;
    @FXML
    private TextField messageInput;
    @FXML
    private Button sendButton;
    @FXML
    private Button analyzeTasksButton;
    @FXML
    private Button createStudyPlanButton;
    @FXML
    private Button pomodoroAdviceButton;
    @FXML
    private Label statusLabel;
    @FXML
    private Label usernameLabel;
    @FXML
    private ProgressIndicator loadingIndicator;
    @FXML
    private ScrollPane chatScrollPane;

    private String currentUsername;
    private AIService aiService;
    private List<com.example.chronopanthers.Task> userTasks;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Initialize AI service
        aiService = new AIService();

        // Setup UI
        setupUI();

        // Test API connection
        testConnection();
    }

    private void setupUI() {
        // Make chat history non-editable
        chatHistory.setEditable(false);
        chatHistory.setWrapText(true);

        // Hide loading indicator initially
        loadingIndicator.setVisible(false);

        // Setup auto-scroll for chat
        chatHistory.textProperty().addListener((obs, oldText, newText) -> {
            Platform.runLater(() -> {
                chatScrollPane.setVvalue(1.0);
            });
        });

        // Enable send button only when message is not empty
        messageInput.textProperty().addListener((obs, oldText, newText) -> {
            sendButton.setDisable(newText.trim().isEmpty());
        });

        // Allow Enter key to send message
        messageInput.setOnAction(e -> sendMessage());

        // Initial status
        statusLabel.setText("Study & Focus AI ready");

        // Welcome message
        appendToChatHistory("🎓 Study AI",
                "Hello! I'm your Study & Focus AI Assistant. I specialize in:\n" +
                        "• Creating personalized study plans\n" +
                        "• Optimizing Pomodoro sessions\n" +
                        "• Managing task priorities and deadlines\n" +
                        "• Providing focus and productivity strategies\n\n" +
                        "How can I help you achieve your study goals today?", false);
    }

    private void testConnection() {
        Task<Boolean> connectionTask = new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                return aiService.testConnection();
            }

            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    if (getValue()) {
                        statusLabel.setText("✓ Connected to Study AI");
                        statusLabel.setStyle("-fx-text-fill: green;");
                    } else {
                        statusLabel.setText("⚠ AI service connection failed");
                        statusLabel.setStyle("-fx-text-fill: orange;");
                    }
                });
            }

            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    statusLabel.setText("✗ Failed to connect to AI service");
                    statusLabel.setStyle("-fx-text-fill: red;");
                });
            }
        };

        Thread connectionThread = new Thread(connectionTask);
        connectionThread.setDaemon(true);
        connectionThread.start();
    }

    public void setCurrentUser(String username) {
        this.currentUsername = username;
        if (usernameLabel != null) {
            usernameLabel.setText("Study AI for: " + username);
        }

        // Load user's tasks for analysis
        loadUserTasks();

        // Add personalized welcome message
        String personalizedMessage = String.format(
                "Welcome back, %s! 🎯\n\n" +
                        "I've loaded your current tasks and I'm ready to help you:\n" +
                        "• Plan your study sessions with Pomodoro technique\n" +
                        "• Prioritize tasks based on deadlines and importance\n" +
                        "• Create focused work schedules\n" +
                        "• Provide study tips and motivation\n" +
                        "• Add new tasks directly to your task manager\n\n" +
                        "💡 **Quick Commands:**\n" +
                        "• Type: \"add task: [Task Name], [Date]\" to add tasks\n" +
                        "• Example: \"add task: Math Assignment, July 2 2025\"\n" +
                        "• Use the quick action buttons below for instant help!\n\n" +
                        "What would you like to work on today?", username);

        appendToChatHistory("🎓 Study AI", personalizedMessage, false);
    }

    private void loadUserTasks() {
        if (currentUsername != null) {
            userTasks = TaskDatabaseManager.getUserTasks(currentUsername);
            System.out.println("Loaded " + userTasks.size() + " tasks for AI analysis");
        }
    }

    @FXML
    private void sendMessage() {
        String message = messageInput.getText().trim();
        if (message.isEmpty()) {
            return;
        }

        // Clear input and show loading
        messageInput.clear();
        setUILoading(true);

        // Add user message to chat
        appendToChatHistory("👤 You", message, true);

        // Create background task for AI response
        Task<String> aiTask = new Task<String>() {
            @Override
            protected String call() throws Exception {
                return aiService.getStudyFocusResponse(message, currentUsername, userTasks);
            }

            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    String response = getValue();
                    appendToChatHistory("🎓 Study AI", response, false);

                    // Refresh tasks if a task might have been added
                    if (message.toLowerCase().contains("add task:")) {
                        loadUserTasks();
                    }

                    setUILoading(false);
                });
            }

            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    appendToChatHistory("⚠️ Error",
                            "I'm having trouble connecting right now. Please try again in a moment.", false);
                    setUILoading(false);
                });
            }
        };

        Thread aiThread = new Thread(aiTask);
        aiThread.setDaemon(true);
        aiThread.start();
    }

    @FXML
    private void analyzeTasksAndPriorities() {
        if (currentUsername == null) {
            appendToChatHistory("⚠️ System", "Please make sure you're logged in.", false);
            return;
        }

        loadUserTasks(); // Refresh tasks

        if (userTasks.isEmpty()) {
            appendToChatHistory("📋 Task Analysis",
                    "You don't have any tasks yet. Would you like me to help you create some tasks or set up a study plan?", false);
            return;
        }

        setUILoading(true);

        Task<String> analysisTask = new Task<String>() {
            @Override
            protected String call() throws Exception {
                return aiService.analyzeTasksAndPriorities(userTasks, currentUsername);
            }

            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    appendToChatHistory("📊 Task Analysis", getValue(), false);
                    setUILoading(false);
                });
            }

            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    appendToChatHistory("⚠️ Error", "Failed to analyze tasks. Please try again.", false);
                    setUILoading(false);
                });
            }
        };

        Thread analysisThread = new Thread(analysisTask);
        analysisThread.setDaemon(true);
        analysisThread.start();
    }

    @FXML
    private void createStudyPlan() {
        if (currentUsername == null) {
            appendToChatHistory("⚠️ System", "Please make sure you're logged in.", false);
            return;
        }

        loadUserTasks(); // Refresh tasks
        setUILoading(true);

        Task<String> studyPlanTask = new Task<String>() {
            @Override
            protected String call() throws Exception {
                return aiService.createStudyPlan(userTasks, currentUsername);
            }

            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    appendToChatHistory("📅 Study Plan", getValue(), false);
                    setUILoading(false);
                });
            }

            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    appendToChatHistory("⚠️ Error", "Failed to create study plan. Please try again.", false);
                    setUILoading(false);
                });
            }
        };

        Thread studyPlanThread = new Thread(studyPlanTask);
        studyPlanThread.setDaemon(true);
        studyPlanThread.start();
    }

    @FXML
    private void getPomodoroAdvice() {
        setUILoading(true);

        Task<String> pomodoroTask = new Task<String>() {
            @Override
            protected String call() throws Exception {
                return aiService.getPomodoroAdvice(userTasks, currentUsername);
            }

            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    appendToChatHistory("🍅 Pomodoro Guide", getValue(), false);
                    setUILoading(false);
                });
            }

            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    appendToChatHistory("⚠️ Error", "Failed to get Pomodoro advice. Please try again.", false);
                    setUILoading(false);
                });
            }
        };

        Thread pomodoroThread = new Thread(pomodoroTask);
        pomodoroThread.setDaemon(true);
        pomodoroThread.start();
    }

    private void setUILoading(boolean loading) {
        loadingIndicator.setVisible(loading);
        sendButton.setDisable(loading || messageInput.getText().trim().isEmpty());
        analyzeTasksButton.setDisable(loading);
        createStudyPlanButton.setDisable(loading);
        pomodoroAdviceButton.setDisable(loading);

        if (loading) {
            statusLabel.setText("AI is thinking...");
            statusLabel.setStyle("-fx-text-fill: blue;");
        } else {
            statusLabel.setText("Ready to help");
            statusLabel.setStyle("-fx-text-fill: green;");
        }
    }

    private void appendToChatHistory(String sender, String message, boolean isUser) {
        String timestamp = java.time.LocalTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"));
        String formattedMessage = String.format("[%s] %s:\n%s\n\n", timestamp, sender, message);

        Platform.runLater(() -> {
            chatHistory.appendText(formattedMessage);
        });
    }

    // Navigation methods
    @FXML
    public void goToTimer(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("timer.fxml"));
        Parent root = loader.load();

        Controller timerController = loader.getController();
        if (currentUsername != null) {
            timerController.setCurrentUser(currentUsername);
        }

        Stage stage = getStageFromEvent(event);
        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/com/example/chronopanthers/timer.css").toExternalForm());
        stage.setTitle("Pomodoro Timer");
        stage.setScene(scene);
        stage.show();
    }

    @FXML
    public void goToTaskManager(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("taskManager.fxml"));
        Parent root = loader.load();

        TaskManager taskManagerController = loader.getController();
        if (currentUsername != null) {
            taskManagerController.setCurrentUser(currentUsername);
        }

        Stage stage = getStageFromEvent(event);
        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/com/example/chronopanthers/taskManager.css").toExternalForm());
        stage.setTitle("Task Manager");
        stage.setScene(scene);
        stage.show();
    }

    @FXML
    public void logout(ActionEvent event) throws IOException {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Logout");
        alert.setHeaderText("You're about to logout!");
        alert.setContentText("Your chat history will be cleared. Continue?");

        if(alert.showAndWait().get() == ButtonType.OK){
            Parent root = FXMLLoader.load(getClass().getResource("loginPage.fxml"));
            Stage stage = getStageFromEvent(event);
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/com/example/chronopanthers/loginPage.css").toExternalForm());
            stage.setTitle("Login Page");
            stage.setScene(scene);
            stage.show();
        }
    }

    // Helper method to get Stage from different event sources
    private Stage getStageFromEvent(ActionEvent event) {
        Object source = event.getSource();

        if (source instanceof MenuItem) {
            // For menu items
            MenuItem menuItem = (MenuItem) source;
            return (Stage) menuItem.getParentPopup().getOwnerWindow();
        } else if (source instanceof Node) {
            // For buttons and other nodes
            Node node = (Node) source;
            return (Stage) node.getScene().getWindow();
        } else {
            // Fallback - try to get from any node in the scene
            throw new IllegalArgumentException("Unable to determine stage from event source: " + source.getClass());
        }
    }
}