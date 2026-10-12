package com.wipro.scheduler.task;

import com.wipro.scheduler.util.TaskExecutionException;

/**
 * Simulates a long-running report generation job.
 */
public class ReportGenerationTask implements Task {

    private static final long EXECUTION_TIME_MS = 1500;

    private final String name;
    private final boolean simulateFailure;
    private int priority;

    public ReportGenerationTask(String name, int priority) {
        this(name, priority, false);
    }

    public ReportGenerationTask(String name, int priority, boolean simulateFailure) {
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
            throw new TaskExecutionException("Report task '" + name + "' was interrupted", e);
        }
        if (simulateFailure) {
            throw new TaskExecutionException("Report task '" + name + "' failed during generation");
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
        return "ReportGenerationTask{" + name + ", priority=" + priority + "}";
    }
}
