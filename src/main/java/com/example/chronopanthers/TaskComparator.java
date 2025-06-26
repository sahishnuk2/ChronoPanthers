package com.example.chronopanthers;

import java.util.Comparator;

public class TaskComparator implements Comparator<Task> {
    public enum SortMode {DEADLINE_FIRST, NAME, PRIORITY, SUBJECT}

    private SortMode mode;

    public TaskComparator(SortMode mode) {
        this.mode = mode;
    }

    @Override
    public int compare(Task o1, Task o2) {
        if (o1.getIsCompleted() && !o2.getIsCompleted()) {
            return 1; // o1 is completed, but o2 is not
        } else if (!o1.getIsCompleted() && o2.getIsCompleted()) {
            return -1; //o1 not completed, but o2 is
        } else if (mode == SortMode.DEADLINE_FIRST) {
            // deadline tasks before normal task
            // deadline tasks to be sorted by deadline
            // completed tasks to be at the bottom
            if (o1 instanceof DeadlineTask && !(o2 instanceof DeadlineTask)) {
                // both completed
                // or both not completed
                return -1; // o1 is deadline task, o2 is not;
            } else if (!(o1 instanceof DeadlineTask) && o2 instanceof DeadlineTask) {
                // both completed
                // or both not completed
                return 1; // o2 is deadline task, o1 is not;
            } else if (o1 instanceof DeadlineTask && o2 instanceof DeadlineTask) {
                // both deadline
                return o1.getDeadline().compareTo(o2.getDeadline());
            } else {
                //both normal
                return 0;
            }
        } else if (mode == SortMode.NAME) {
            // alphabetically sort
            return o1.getTaskName().compareToIgnoreCase(o2.getTaskName());
        } else if (mode == SortMode.PRIORITY) {
                // both completed
                // or both not completed
                return Integer.compare(o1.getPriority().getLevel(), o2.getPriority().getLevel());
        } else if (mode == SortMode.SUBJECT) {
            // Still TO DO
            return 0;
        }
        return 0;
    }
}
