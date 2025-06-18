package com.example.chronopanthers;


import java.time.LocalDate;

public class DeadlineTask extends Task{
    private LocalDate deadline;
    private boolean isOverdue = false;

    public DeadlineTask(String taskName, int year, int month, int day) {
        super(taskName);
        this.deadline = LocalDate.of(year, month, day);
    }

    @Override
    public boolean isOverdue() {
        this.isOverdue = LocalDate.now().isAfter(this.deadline) && !this.isCompleted();
        return this.isOverdue;
    }

    @Override
    public LocalDate getDeadline() {
        return this.deadline;
    }

    @Override
    public String getTaskType() {
        return "Deadline";
    }

    @Override
    public String toString() {
        return String.format("[ %s | %b | %s | %b ]",
                this.getTaskName(), this.isCompleted(), this.deadline, this.isOverdue());
    }


}