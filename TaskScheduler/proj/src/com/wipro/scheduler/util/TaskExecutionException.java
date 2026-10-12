package com.wipro.scheduler.util;

/**
 * Thrown when a task fails during execution.
 */
public class TaskExecutionException extends Exception {

    private static final long serialVersionUID = 1L;

    public TaskExecutionException(String message) {
        super(message);
    }

    public TaskExecutionException(String message, Throwable cause) {
        super(message, cause);
    }

    @Override
    public String toString() {
        return "TaskExecutionException: " + getMessage();
    }
}
