package com.example.chronopanthers;

import java.time.LocalDate;

public class NormalTask extends Task {
    public NormalTask(String taskName) {
        super(taskName);
    }

    @Override
    public String getTaskType() {
        return "Normal";
    }

    @Override
    public boolean getIsOverdue() {
        return false;
    }

    @Override
    public LocalDate getDeadline() {
       return null;
    }

    @Override
    public String toString() {
        return String.format("[ %s | %b | NIL | NIL]", this.getTaskName(), this.getIsCompleted());
    }
}
