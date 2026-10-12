package com.wipro.scheduler.task;

import com.wipro.scheduler.util.TaskExecutionException;

/**
 * Simulates a quick email-sending job.
 */
public class EmailNotificationTask implements Task {

    private static final long EXECUTION_TIME_MS = 300;

    private final String name;
    private final boolean simulateFailure;
    private int priority;

    public EmailNotificationTask(String name, int priority) {
        this(name, priority, false);
    }

    public EmailNotificationTask(String name, int priority, boolean simulateFailure) {
        this.name = name;
        this.priority = priority;
        this.simulateFailure = simulateFailure;
    }

    @Override
    public void execute() throws TaskExecutionException {
        System.out.println("[" + Thread.currentThread().getName() + "] START  "
                + name + " (priority " + priority + ")");
        try {
            Thread.sleep(EXECUTION_TIME_MS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new TaskExecutionException("Email task '" + name + "' was interrupted", e);
        }
        if (simulateFailure) {
            throw new TaskExecutionException("Email task '" + name + "' failed to send");
        }
        System.out.println("[" + Thread.currentThread().getName() + "] FINISH "
                + name + " (priority " + priority + ")");
    }

    @Override
    public int getPriority() {
        return priority;
    }

    @Override
    public String toString() {
        return "EmailNotificationTask{" + name + ", priority=" + priority + "}";
    }
}
