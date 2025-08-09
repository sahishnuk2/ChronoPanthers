package com.example.chronopanthers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class AIService {
    private static final String OPENROUTER_API_URL = "https://openrouter.ai/api/v1/chat/completions";
    private static final String MODEL_NAME = "deepseek/deepseek-r1-distill-llama-70b";
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String apiKey;

    // Store pending task suggestions for confirmation
    private static final Map<String, PendingTaskSuggestion> pendingTasks = new HashMap<>();

    public AIService() {
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
        this.apiKey = AIConfig.getApiKey();
    }

    public boolean testConnection() {
        try {
            String response = makeAIRequest("Hello, this is a test message. Please respond with 'Connection successful'.");
            return response != null && !response.isEmpty();
        } catch (Exception e) {
            System.err.println("AI Service connection test failed: " + e.getMessage());
            return false;
        }
    }

    public String getStudyFocusResponse(String userMessage, String username, List<Task> userTasks) throws IOException, InterruptedException {
        // Check if user wants to add a task
        TaskAddResult taskResult = checkAndAddTask(userMessage, username);
        if (taskResult.wasTaskAdded) {
            return taskResult.response;
        }

        // Check for pending task additions (user confirming suggested tasks)
        String confirmationResponse = handleTaskConfirmation(userMessage, username);
        if (confirmationResponse != null) {
            return confirmationResponse;
        }

        // Check if message is study/work related
        if (!isStudyWorkRelated(userMessage)) {
            return "I'm sorry, this is not within my programming. I can only help with your Study/Work planning and Task Management.";
        }

        String systemPrompt = createStudyFocusSystemPrompt(username, userTasks);
        String aiResponse = makeAIRequest(userMessage, systemPrompt);

        // Check if AI is suggesting task additions and modify response accordingly
        return enhanceResponseWithTaskSuggestions(aiResponse);
    }

    public String analyzeTasksAndPriorities(List<Task> tasks, String username) throws IOException, InterruptedException {
        if (tasks.isEmpty()) {
            return "You don't have any tasks to analyze yet. Would you like me to help you create a study plan or add some tasks?";
        }

        String taskSummary = formatTasksForAI(tasks);
        String prompt = String.format(
                "Analyze these tasks for %s and provide:\n" +
                        "1. Priority ranking based on deadlines and importance\n" +
                        "2. Risk assessment for overdue/approaching deadlines\n" +
                        "3. Suggested order of completion\n" +
                        "4. Time allocation recommendations\n" +
                        "5. Pomodoro session planning\n\n" +
                        "Current tasks:\n%s\n\n" +
                        "Today's date: %s\n\n" +
                        "Provide actionable, specific advice for managing these tasks effectively.",
                username, taskSummary, LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM d, yyyy"))
        );

        return makeAIRequest(prompt, createStudyFocusSystemPrompt(username, tasks));
    }

    public String createStudyPlan(List<Task> tasks, String username) throws IOException, InterruptedException {
        String taskSummary = formatTasksForAI(tasks);
        String prompt = String.format(
                "Create a comprehensive study plan for %s based on these tasks:\n\n%s\n\n" +
                        "Include:\n" +
                        "1. Daily schedule with specific time blocks\n" +
                        "2. Pomodoro session breakdowns (25min work + 5min break)\n" +
                        "3. Priority-based task sequencing\n" +
                        "4. Break recommendations and activities\n" +
                        "5. Weekly milestones and checkpoints\n" +
                        "6. Flexibility for unexpected tasks\n" +
                        "7. Study techniques for different task types\n\n" +
                        "Today's date: %s\n" +
                        "Make it practical, realistic, and motivating!",
                username, taskSummary, LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM d, yyyy"))
        );

        return makeAIRequest(prompt, createStudyPlanSystemPrompt());
    }

    public String getPomodoroAdvice(List<Task> tasks, String username) throws IOException, InterruptedException {
        String taskSummary = formatTasksForAI(tasks);
        String prompt = String.format(
                "Provide personalized Pomodoro technique advice for %s based on their current tasks:\n\n%s\n\n" +
                        "Include:\n" +
                        "1. Optimal Pomodoro session length for different task types\n" +
                        "2. How to break down large tasks into Pomodoro sessions\n" +
                        "3. Best break activities for maintaining focus\n" +
                        "4. Tips for handling interruptions and distractions\n" +
                        "5. When to take longer breaks vs short breaks\n" +
                        "6. Strategies for maintaining motivation throughout the day\n" +
                        "7. How to adjust technique based on task difficulty\n\n" +
                        "Make it actionable and specific to their current workload!",
                username, taskSummary
        );

        return makeAIRequest(prompt, createPomodoroSystemPrompt());
    }

    private String makeAIRequest(String userMessage) throws IOException, InterruptedException {
        return makeAIRequest(userMessage, createDefaultSystemPrompt());
    }

    private String makeAIRequest(String userMessage, String systemPrompt) throws IOException, InterruptedException {
        // Create request body
        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("model", MODEL_NAME);

        ArrayNode messages = objectMapper.createArrayNode();

        // Add system message
        ObjectNode systemMessage = objectMapper.createObjectNode();
        systemMessage.put("role", "system");
        systemMessage.put("content", systemPrompt);
        messages.add(systemMessage);

        // Add user message
        ObjectNode userMessageNode = objectMapper.createObjectNode();
        userMessageNode.put("role", "user");
        userMessageNode.put("content", userMessage);
        messages.add(userMessageNode);

        requestBody.set("messages", messages);
        requestBody.put("max_tokens", 3000);
        requestBody.put("temperature", 0.7);

        // Create HTTP request
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(OPENROUTER_API_URL))
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .header("HTTP-Referer", "https://github.com/chronopanthers/study-app")
                .header("X-Title", "ChronoPanthers Study Assistant")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
                .build();

        // Send request
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new IOException("API request failed with status: " + response.statusCode() + "\nResponse: " + response.body());
        }

        // Parse response
        JsonNode responseJson = objectMapper.readTree(response.body());
        JsonNode choices = responseJson.get("choices");

        if (choices != null && choices.isArray() && choices.size() > 0) {
            JsonNode firstChoice = choices.get(0);
            JsonNode message = firstChoice.get("message");
            if (message != null) {
                JsonNode content = message.get("content");
                if (content != null) {
                    return content.asText();
                }
            }
        }

        throw new IOException("Invalid response format from AI service");
    }

    private String formatTasksForAI(List<Task> tasks) {
        if (tasks.isEmpty()) {
            return "No tasks available.";
        }

        StringBuilder sb = new StringBuilder();
        LocalDate today = LocalDate.now();

        sb.append("=== TASK SUMMARY ===\n");
        sb.append("Total tasks: ").append(tasks.size()).append("\n");

        long completedTasks = tasks.stream().filter(Task::getIsCompleted).count();
        long pendingTasks = tasks.size() - completedTasks;
        sb.append("Completed: ").append(completedTasks).append(" | Pending: ").append(pendingTasks).append("\n\n");

        // Group tasks by type and status
        List<Task> overdueTasks = tasks.stream()
                .filter(task -> task instanceof DeadlineTask && task.getIsOverdue() && !task.getIsCompleted())
                .collect(Collectors.toList());

        List<Task> upcomingDeadlines = tasks.stream()
                .filter(task -> task instanceof DeadlineTask && !task.getIsCompleted() && !task.getIsOverdue())
                .sorted((a, b) -> a.getDeadline().compareTo(b.getDeadline()))
                .collect(Collectors.toList());

        List<Task> normalTasks = tasks.stream()
                .filter(task -> task instanceof NormalTask && !task.getIsCompleted())
                .collect(Collectors.toList());

        if (!overdueTasks.isEmpty()) {
            sb.append("🚨 OVERDUE TASKS (URGENT!):\n");
            for (Task task : overdueTasks) {
                sb.append("- ").append(task.getTaskName())
                        .append(" [").append(task.getPriority()).append("]")
                        .append(" (Due: ").append(task.getDeadline().format(DateTimeFormatter.ofPattern("MMM d")))
                        .append(")\n");
            }
            sb.append("\n");
        }

        if (!upcomingDeadlines.isEmpty()) {
            sb.append("📅 UPCOMING DEADLINES:\n");
            for (Task task : upcomingDeadlines) {
                long daysUntilDue = ChronoUnit.DAYS.between(today, task.getDeadline());
                sb.append("- ").append(task.getTaskName())
                        .append(" [").append(task.getPriority()).append("]")
                        .append(" (Due: ").append(task.getDeadline().format(DateTimeFormatter.ofPattern("MMM d")))
                        .append(" - ").append(daysUntilDue).append(" days)\n");
            }
            sb.append("\n");
        }

        if (!normalTasks.isEmpty()) {
            sb.append("📋 OTHER TASKS:\n");
            for (Task task : normalTasks) {
                sb.append("- ").append(task.getTaskName())
                        .append(" [").append(task.getPriority()).append("]\n");
            }
            sb.append("\n");
        }

        return sb.toString();
    }

    private String createStudyFocusSystemPrompt(String username, List<Task> tasks) {
        return String.format(
                "You are a Study & Focus AI Assistant for %s. You specialize ONLY in:\n" +
                        "- Study planning and academic productivity\n" +
                        "- Pomodoro technique optimization\n" +
                        "- Task prioritization and deadline management\n" +
                        "- Focus strategies and motivation\n" +
                        "- Time management for students and professionals\n" +
                        "- Work/study schedule creation\n" +
                        "- Task organization and planning\n\n" +

                        "IMPORTANT RESTRICTIONS:\n" +
                        "- You ONLY help with study, work, and productivity topics\n" +
                        "- Do NOT answer math/science homework questions\n" +
                        "- Do NOT provide solutions to assignments\n" +
                        "- Do NOT discuss unrelated topics (entertainment, politics, etc.)\n" +
                        "- Focus on HOW to study/work, not WHAT the answers are\n\n" +

                        "CURRENT USER CONTEXT:\n" +
                        "- Username: %s\n" +
                        "- Total tasks: %d\n" +
                        "- Today's date: %s\n\n" +

                        "YOUR COMMUNICATION STYLE:\n" +
                        "- Be encouraging and motivational\n" +
                        "- Provide specific, actionable advice\n" +
                        "- When suggesting tasks, ask if user wants to add them\n" +
                        "- Use emojis appropriately for engagement\n" +
                        "- Break down complex advice into clear steps\n" +
                        "- Reference their specific tasks when relevant\n" +
                        "- Focus on practical solutions they can implement immediately\n\n" +

                        "TASK SUGGESTIONS:\n" +
                        "- When you suggest tasks during planning, always ask:\n" +
                        "  'Would you like me to add any of these tasks to your Task Manager?'\n" +
                        "- For deadline tasks: 'add task: [task name], [date]'\n" +
                        "- For normal tasks: 'add task: [task name]'\n" +
                        "- Example: 'add task: Math Assignment, July 2 2025' or 'add task: Review notes'\n\n" +

                        "ALWAYS CONSIDER:\n" +
                        "- Their current task load and priorities\n" +
                        "- Realistic time management given their schedule\n" +
                        "- Pomodoro technique integration\n" +
                        "- Study/work-life balance\n" +
                        "- Stress management and burnout prevention",

                username, username, tasks.size(), LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM d, yyyy"))
        );
    }

    private String createStudyPlanSystemPrompt() {
        return "You are an expert study planner and academic coach. Create detailed, realistic study plans that:\n" +
                "- Use the Pomodoro technique (25min focus + 5min break)\n" +
                "- Account for different learning styles and task types\n" +
                "- Include specific time blocks and schedules\n" +
                "- Provide flexibility for unexpected events\n" +
                "- Balance intensive study with proper rest\n" +
                "- Include motivational checkpoints and rewards\n" +
                "- Consider deadlines and priority levels\n" +
                "- Suggest specific study techniques for different subjects\n\n" +
                "Format your plans clearly with time blocks, task breakdowns, and practical tips.";
    }

    private String createPomodoroSystemPrompt() {
        return "You are a Pomodoro technique expert and productivity coach. Provide detailed advice on:\n" +
                "- Optimizing Pomodoro sessions for different task types\n" +
                "- Breaking large projects into manageable 25-minute chunks\n" +
                "- Choosing effective break activities\n" +
                "- Handling interruptions and maintaining focus\n" +
                "- Adapting the technique for different work styles\n" +
                "- Tracking progress and maintaining motivation\n" +
                "- When to take longer breaks vs. standard 5-minute breaks\n\n" +
                "Always provide specific, actionable strategies that users can implement immediately.";
    }

    private String createDefaultSystemPrompt() {
        return "You are a helpful Study & Focus AI Assistant. You help users with productivity, time management, " +
                "study planning, and task organization. Be encouraging, practical, and provide specific actionable advice. " +
                "Use the Pomodoro technique and evidence-based productivity strategies in your recommendations.";
    }

    // Task addition functionality
    private static class TaskAddResult {
        boolean wasTaskAdded;
        String response;

        TaskAddResult(boolean wasTaskAdded, String response) {
            this.wasTaskAdded = wasTaskAdded;
            this.response = response;
        }
    }

    private TaskAddResult checkAndAddTask(String userMessage, String username) {
        // Pattern to match: "add task: [task name]" with optional date
        Pattern patternWithDate = Pattern.compile("add task:\\s*([^,]+),\\s*(.+)", Pattern.CASE_INSENSITIVE);
        Pattern patternWithoutDate = Pattern.compile("add task:\\s*(.+)", Pattern.CASE_INSENSITIVE);

        Matcher matcherWithDate = patternWithDate.matcher(userMessage.trim());
        Matcher matcherWithoutDate = patternWithoutDate.matcher(userMessage.trim());

        String taskName;
        LocalDate deadline = null;
        Task.Priority priority;
        Task newTask;

        if (matcherWithDate.matches()) {
            // Format: "add task: [task name], [date]"
            taskName = matcherWithDate.group(1).trim();
            String dateString = matcherWithDate.group(2).trim();

            // Check if task already exists before parsing date
            if (TaskDatabaseManager.taskExists(username, taskName)) {
                return new TaskAddResult(true,
                        "⚠️ **Task Already Exists!**\n\n" +
                                "A task with the name '" + taskName + "' already exists in your task list.\n\n" +
                                "**Options:**\n" +
                                "• Use a different name for this task\n" +
                                "• Check your Task Manager to see the existing task\n" +
                                "• Try: 'add task: " + taskName + " (Version 2), " + dateString + "'\n\n" +
                                "Would you like to suggest a different name for this task?");
            }

            // Try to parse the date
            deadline = parseDate(dateString);
            if (deadline == null) {
                return new TaskAddResult(true,
                        "❌ I couldn't parse the date '" + dateString + "'. Please use formats like:\n" +
                                "• 2 July 2025\n" +
                                "• July 2, 2025\n" +
                                "• 2025-07-02\n" +
                                "• 02/07/2025\n\n" +
                                "Or just use 'add task: [task name]' for a normal task without deadline.");
            }

            // Calculate priority based on deadline proximity
            priority = calculatePriorityFromDeadline(deadline);
            newTask = new DeadlineTask(taskName, deadline, priority);

        } else if (matcherWithoutDate.matches()) {
            // Format: "add task: [task name]" (no date)
            taskName = matcherWithoutDate.group(1).trim();

            // If task already exists, inform the user (don't add the task)
            if (TaskDatabaseManager.taskExists(username, taskName)) {
                return new TaskAddResult(true,
                        "⚠️ **Task Already Exists!**\n\n" +
                                "A task with the name '" + taskName + "' already exists in your task list.\n\n" +
                                "**Options:**\n" +
                                "• Use a different name for this task\n" +
                                "• Check your Task Manager to see the existing task\n" +
                                "• Try: 'add task: " + taskName + " (Version 2)'\n\n" +
                                "Would you like to suggest a different name for this task?");
            }

            // Create normal task with default priority
            priority = Task.Priority.MEDIUM; // Default priority for normal tasks
            newTask = new NormalTask(taskName, priority);

        } else {
            // No match for either pattern
            return new TaskAddResult(false, "");
        }

        // Add the task to database
        boolean success = TaskDatabaseManager.addTask(username, newTask);

        if (success) {
            if (deadline != null) {
                // Deadline task success message
                long daysUntilDue = ChronoUnit.DAYS.between(LocalDate.now(), deadline);
                String urgencyText = getUrgencyText(daysUntilDue);

                return new TaskAddResult(true,
                        "✅ **Deadline Task Added Successfully!**\n\n" +
                                "📋 **Task Details:**\n" +
                                "• **Name:** " + taskName + "\n" +
                                "• **Deadline:** " + deadline.format(DateTimeFormatter.ofPattern("MMMM d, yyyy")) + "\n" +
                                "• **Priority:** " + priority + " " + urgencyText + "\n" +
                                "• **Days until due:** " + daysUntilDue + " days\n\n" +
                                "The deadline task has been added to your Task Manager successfully! 🎯");
            } else {
                // Normal task success message
                return new TaskAddResult(true,
                        "✅ **Normal Task Added Successfully!**\n\n" +
                                "📋 **Task Details:**\n" +
                                "• **Name:** " + taskName + "\n" +
                                "• **Type:** Normal Task (no deadline)\n" +
                                "• **Priority:** " + priority + "\n\n" +
                                "The task has been added to your Task Manager successfully! 📝");
            }
        } else {
            // Database error - but this could also be due to task already existing
            // Check if it's because the task already exists
            if (TaskDatabaseManager.taskExists(username, taskName)) {
                return new TaskAddResult(true,
                        "⚠️ **Task Already Exists!**\n\n" +
                                "A task with the name '" + taskName + "' already exists in your task list.\n\n" +
                                "This might have been added recently. Please check your Task Manager or try using a different name for this task.");
            } else {
                return new TaskAddResult(true,
                        "❌ **Failed to Add Task**\n\n" +
                                "I encountered an error while adding '" + taskName + "' to your task list. This might be because:\n" +
                                "• Database connection issue\n" +
                                "• Invalid task data\n" +
                                "• Server error\n\n" +
                                "Please try again or add the task manually through the Task Manager.");
            }
        }
    }

    private LocalDate parseDate(String dateString) {
        // Date Formats Allowed
        String[] patterns = {
                "d MMMM yyyy",   // 2 July 2025
                "MMMM d, yyyy",  // July 2, 2025
                "MMMM d yyyy",   // July 2 2025
                "yyyy-MM-dd",    // 2025-07-02
                "dd/MM/yyyy",    // 02/07/2025
                "MM/dd/yyyy",    // 07/02/2025
                "d-M-yyyy",      // 2-7-2025
                "d.M.yyyy",      // 2.7.2025
                "d MMM yyyy",    // 2 Jul 2025
                "MMM d, yyyy",   // Jul 2, 2025
                "MMM d yyyy"     // Jul 2 2025
        };

        for (String str : patterns) {
            DateTimeFormatter formatter = new DateTimeFormatterBuilder()
                    .parseCaseInsensitive() // Allows for lower case for the month
                    .appendPattern(str)
                    .toFormatter();

            try {
                return LocalDate.parse(dateString, formatter);
            } catch (DateTimeParseException e) {
                // Try next format
            }
        }
        return null; // Could not parse
    }

    private Task.Priority calculatePriorityFromDeadline(LocalDate deadline) {
        long daysUntilDue = ChronoUnit.DAYS.between(LocalDate.now(), deadline);

        if (daysUntilDue < 0) {
            return Task.Priority.CRITICAL; // Overdue
        } else if (daysUntilDue <= 1) {
            return Task.Priority.CRITICAL; // Due today or tomorrow
        } else if (daysUntilDue <= 3) {
            return Task.Priority.HIGH; // Due within 3 days
        } else if (daysUntilDue <= 7) {
            return Task.Priority.MEDIUM; // Due within a week
        } else if (daysUntilDue <= 14) {
            return Task.Priority.LOW; // Due within 2 weeks
        } else {
            return Task.Priority.NONE; // Due in more than 2 weeks
        }
    }

    private String getUrgencyText(long daysUntilDue) {
        if (daysUntilDue < 0) {
            return "⚠️ (OVERDUE!)";
        } else if (daysUntilDue <= 1) {
            return "🔥 (Very Urgent!)";
        } else if (daysUntilDue <= 3) {
            return "⚡ (Urgent)";
        } else if (daysUntilDue <= 7) {
            return "📅 (This Week)";
        } else if (daysUntilDue <= 14) {
            return "📆 (Next 2 Weeks)";
        } else {
            return "🗓️ (Future)";
        }
    }

    // Feature 1: Study/Work content filtering
    private boolean isStudyWorkRelated(String message) {
        String lowerMessage = message.toLowerCase();

        // List of non-study/work related keywords that should be blocked
        String[] blockedKeywords = {
                "what is life", "meaning of life", "philosophy", "religion", "politics",
                "weather", "jokes", "funny", "entertainment", "movies", "games",
                "cooking", "recipes", "sports", "news", "current events",
                "personal relationships", "dating", "health", "medical advice",
                "legal advice", "financial investment", "cryptocurrency"
        };

        // List of study/work related keywords that should be allowed
        String[] allowedKeywords = {
                "study", "work", "task", "assignment", "project", "exam", "test",
                "deadline", "schedule", "plan", "focus", "productivity", "pomodoro",
                "time management", "organization", "priority", "goal", "homework",
                "research", "learning", "concentration", "break", "session",
                "motivation", "efficiency", "progress", "academic", "school",
                "university", "college", "subject", "course", "syllabus"
        };

        // Check for blocked content
        for (String blocked : blockedKeywords) {
            if (lowerMessage.contains(blocked)) {
                return false;
            }
        }

        // If message contains math/science questions without study context
        if (lowerMessage.matches(".*solve.*\\d+.*") ||
                lowerMessage.matches(".*calculate.*") ||
                lowerMessage.matches(".*what is.*\\+.*") ||
                lowerMessage.matches(".*formula.*for.*")) {
            // Allow if it's about study planning, block if it's asking for solutions
            return lowerMessage.contains("study") || lowerMessage.contains("plan") ||
                    lowerMessage.contains("schedule") || lowerMessage.contains("time");
        }

        // Check for allowed content
        for (String allowed : allowedKeywords) {
            if (lowerMessage.contains(allowed)) {
                return true;
            }
        }

        // Allow general greetings and AI interaction
        if (lowerMessage.matches(".*\\b(hello|hi|hey|help|thanks|thank you)\\b.*")) {
            return true;
        }

        // Allow questions about the AI's capabilities
        if (lowerMessage.contains("what can you") || lowerMessage.contains("how can you") ||
                lowerMessage.contains("can you help")) {
            return true;
        }

        // Default to false for ambiguous content
        return false;
    }

    // Feature 2: Task suggestion and confirmation system
    private static class PendingTaskSuggestion {
        String taskName;
        LocalDate deadline;
        String username;
        long timestamp;

        PendingTaskSuggestion(String taskName, LocalDate deadline, String username) {
            this.taskName = taskName;
            this.deadline = deadline;
            this.username = username;
            this.timestamp = System.currentTimeMillis();
        }
    }

    private String enhanceResponseWithTaskSuggestions(String aiResponse) {
        // Look for task suggestions in AI response and add confirmation prompts
        if (aiResponse.toLowerCase().contains("suggest") &&
                (aiResponse.toLowerCase().contains("task") || aiResponse.toLowerCase().contains("assignment"))) {

            aiResponse += "\n\n💡 **Would you like me to add any of these suggested tasks to your Task Manager?**\n" +
                    "Just say 'yes, add [task name] by [date]' and I'll add it for you!";
        }

        return aiResponse;
    }

    private String handleTaskConfirmation(String userMessage, String username) {
        String lowerMessage = userMessage.toLowerCase().trim();

        // Pattern for confirming task addition: "yes, add [task] by [date]"
        Pattern confirmPattern = Pattern.compile("yes,?\\s*add\\s+(.+?)\\s+by\\s+(.+)", Pattern.CASE_INSENSITIVE);
        Matcher confirmMatcher = confirmPattern.matcher(userMessage.trim());

        if (confirmMatcher.matches()) {
            String taskName = confirmMatcher.group(1).trim();
            String dateString = confirmMatcher.group(2).trim();

            // Parse the date
            LocalDate deadline = parseDate(dateString);
            if (deadline == null) {
                return "❌ I couldn't parse the date '" + dateString + "'. Please use formats like 'July 2 2025' or '2025-07-02'.";
            }

            // Calculate priority and create task
            Task.Priority priority = calculatePriorityFromDeadline(deadline);
            Task newTask = new DeadlineTask(taskName, deadline, priority);
            boolean success = TaskDatabaseManager.addTask(username, newTask);

            if (success) {
                return "✅ **Task Added Successfully!**\n\n" +
                        "The task '" + taskName + "' has been added to your Task Manager with deadline " +
                        deadline.format(DateTimeFormatter.ofPattern("MMMM d, yyyy")) + ". 🎯";
            } else {
                return "❌ Sorry, I couldn't add the task to your Task Manager. Please try again or add it manually.";
            }
        }

        // Simple yes/no responses for general confirmations
        if (lowerMessage.equals("yes") || lowerMessage.equals("y")) {
            return "Great! Please specify which task you'd like to add using the format:\n" +
                    "'yes, add [task name] by [date]'\n\n" +
                    "Example: 'yes, add Math Assignment by July 15 2025'";
        }

        return null; // No confirmation detected
    }
}