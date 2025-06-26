package com.example.chronopanthers;


import java.time.LocalDate;

abstract class Task {
    public enum Priority {
        CRITICAL(1), HIGH(2), MEDIUM(3), LOW(4), NONE(5);

        private final int level;

        Priority(int level) {
            this.level = level;
        }

        public int getLevel() {
            return this.level;
        }

        @Override
        public String toString() {
            return name();
        }

    }

    private String taskName;
    private boolean isCompleted;
    private Priority priority;

    public Task(String taskName, Priority priority) {
        this.taskName = taskName;
        this.isCompleted = false;
        this.priority = priority;
    }

    public boolean getIsCompleted() {
        return this.isCompleted;
    }

    public void complete() {
        this.isCompleted = true;
    }

    public String getTaskName() {
        return this.taskName;
    }

    public Priority getPriority() {
        return this.priority;
    }

    public abstract String getTaskType();
    public abstract LocalDate getDeadline();
    public abstract boolean getIsOverdue();

    @Override
    public abstract String toString();


}