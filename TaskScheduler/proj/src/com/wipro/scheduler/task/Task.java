package com.wipro.scheduler.task;

import com.wipro.scheduler.util.TaskExecutionException;

/**
 * Common contract for all schedulable tasks.
 * A lower priority number means a higher priority (1 = highest).
 */
public interface Task {

    void execute() throws TaskExecutionException;

    int getPriority();
}
