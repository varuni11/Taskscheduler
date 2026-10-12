package com.wipro.scheduler.service;

import com.wipro.scheduler.task.EmailNotificationTask;
import com.wipro.scheduler.task.ReportGenerationTask;
import com.wipro.scheduler.task.Task;
import com.wipro.scheduler.util.TaskExecutionException;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.PriorityBlockingQueue;

/**
 * Manages a PriorityBlockingQueue and a fixed-size pool of worker threads.
 * Lower priority number = executed first.
 *
 * Shutdown uses "poison pill" tasks with the lowest possible priority, so every
 * task already queued is executed before the workers stop.
 */
public class TaskSchedulerService {

    private static final int INITIAL_CAPACITY = 11;

    private final PriorityBlockingQueue<Task> queue =
            new PriorityBlockingQueue<>(INITIAL_CAPACITY, Comparator.comparingInt(Task::getPriority));
    private final List<Thread> workers = new ArrayList<>();
    private boolean started = false;
    private boolean shuttingDown = false;

    /** Sentinel that sorts after every real task and tells a worker to exit. */
    private static final class PoisonPill implements Task {
        @Override
        public void execute() { /* no-op */ }

        @Override
        public int getPriority() {
            return Integer.MAX_VALUE;
        }
    }

    public synchronized void submitTask(Task task) {
        if (task == null) {
            throw new IllegalArgumentException("task must not be null");
        }
        if (shuttingDown) {
            throw new IllegalStateException("Scheduler is shut down; cannot accept new tasks");
        }
        queue.put(task);
        System.out.println("[main] Submitted " + task);
    }

    public synchronized void startWorkers(int poolSize) {
        if (poolSize <= 0) {
            throw new IllegalArgumentException("poolSize must be > 0");
        }
        if (started) {
            throw new IllegalStateException("Workers already started");
        }
        if (shuttingDown) {
            throw new IllegalStateException("Scheduler is shut down");
        }
        started = true;
        for (int i = 1; i <= poolSize; i++) {
            Thread t = new Thread(this::workerLoop, "Worker-" + i);
            workers.add(t);
            t.start();
        }
    }

    private void workerLoop() {
        while (true) {
            Task task;
            try {
                task = queue.take();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
            if (task instanceof PoisonPill) {
                return;
            }
            try {
                task.execute();
            } catch (TaskExecutionException e) {
                // Log and keep the worker (and scheduler) alive.
                System.err.println("[" + Thread.currentThread().getName() + "] ERROR  " + e);
            } catch (RuntimeException e) {
                System.err.println("[" + Thread.currentThread().getName() + "] ERROR  "
                        + new TaskExecutionException("Unexpected failure in " + task, e));
            }
        }
    }

    /**
     * Stops accepting tasks, lets all queued tasks finish, then stops the workers.
     */
    public void shutdown() {
        List<Thread> toJoin;
        synchronized (this) {
            if (shuttingDown) {
                return;
            }
            shuttingDown = true;
            if (!started) {
                return; // nothing is consuming the queue
            }
            for (int i = 0; i < workers.size(); i++) {
                queue.put(new PoisonPill());
            }
            toJoin = new ArrayList<>(workers);
        }
        for (Thread t : toJoin) {
            try {
                t.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
        System.out.println("[main] Scheduler shut down. All queued tasks completed.");
    }

    public static void main(String[] args) {
        TaskSchedulerService scheduler = new TaskSchedulerService();

        // Test case 1 + 2: 5 tasks, mixed priorities (1 = high, 5 = low), one fails.
        scheduler.submitTask(new ReportGenerationTask("Monthly Report", 3));
        scheduler.submitTask(new EmailNotificationTask("Welcome Email", 5));
        scheduler.submitTask(new ReportGenerationTask("Audit Report (fails)", 2, true));
        scheduler.submitTask(new EmailNotificationTask("Alert Email", 1));
        scheduler.submitTask(new EmailNotificationTask("Newsletter", 4));

        scheduler.startWorkers(2);
        scheduler.shutdown();
    }
}
