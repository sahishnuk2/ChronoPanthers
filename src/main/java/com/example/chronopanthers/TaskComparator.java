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
        if (mode == SortMode.DEADLINE_FIRST) {
            // deadline tasks before normal task
            // deadline tasks to be sorted by deadline
            // completed tasks to be at the bottom
            if (o1.getIsCompleted() && !o2.getIsCompleted()) {
                return 1; // o1 is completed, but o2 is not
            } else if (!o1.getIsCompleted() && o2.getIsCompleted()) {
                return -1; //o1 not completed, but o2 is
            } else if (o1 instanceof DeadlineTask && !(o2 instanceof DeadlineTask)) {
                // both completed
                // or both not completed
                return -1; // o1 is deadline task, o2 is not;
            } else if (!(o1 instanceof DeadlineTask) && o2 instanceof DeadlineTask) {
                // both completed
                // or both not completed
                return 1; // o2 is deadline task, o1 is not;
            } else if (o1 instanceof DeadlineTask && o2 instanceof DeadlineTask) {
                // both deadline
                if (o1.getDeadline().isBefore(o2.getDeadline())) {
                    return -1;
                } else {
                    return 1;
                }
            } else {
                //both normal
                return 0;
            }
        }
//        } else if (mode == SortMode.NAME) {
//            return -1;
//        } else if (mode == SortMode.PRIORITY) {
//            return -1;
//        } else if (mode == SortMode.SUBJECT) {
//            return -1;
//        }
        return 0;
    }
}
