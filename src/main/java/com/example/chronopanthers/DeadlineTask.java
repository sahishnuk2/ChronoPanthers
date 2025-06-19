package com.example.chronopanthers;


import java.time.LocalDate;

public class DeadlineTask extends Task{
    private LocalDate deadline;
    private boolean isOverdue = false;

    public DeadlineTask(String taskName, LocalDate deadline) {
        super(taskName);
        this.deadline = deadline;
    }

    @Override
    public boolean getIsOverdue() {
        this.isOverdue = LocalDate.now().isAfter(this.deadline) && !this.getIsCompleted();
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
                this.getTaskName(), this.getIsCompleted(), this.deadline, this.getIsOverdue());
    }


}