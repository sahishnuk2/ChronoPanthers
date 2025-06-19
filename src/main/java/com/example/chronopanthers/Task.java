package com.example.chronopanthers;


import java.time.LocalDate;

abstract class Task {
    private String taskName;
    private boolean isCompleted;

    public Task(String taskName) {
        this.taskName = taskName;
        this.isCompleted = false;
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

    public abstract String getTaskType();
    public abstract LocalDate getDeadline();
    public abstract boolean getIsOverdue();

    @Override
    public abstract String toString();


}