package net.yetamine.lectures.platform.concurrent;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Exchanger;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

/**
 * Demonstrates the use of several less common synchronizers.
 *
 * <p>
 * Pairs of threads meet and exchange a message. This rendezvous runs in loops,
 * all threads are started at the same time and the main threads wait for all of
 * them to finish their rendezvous iterations. To demonstrate semaphores, a pair
 * gets some privacy and other threads must wait until the current pair finishes
 * the rendezvous. You can try running the code with disabled semaphore calls to
 * see the difference and how the exchanger copes with multiple threads at once.
 */
public final class Synchronizers {

    /** The number of tasks to run. */
    private static final int TASK_COUNT = 5;

    /**
     * The number of iteration to take. The iterations use the same threads, but
     * restarts the content for the semaphore for all threads at the same time.
     */
    private static final int ITERATION_COUNT = 2;

    public static void main(String... args) {
        // This semaphore allows two threads to enter only
        final Semaphore pairing = new Semaphore(2);

        // The barrier lets all threads to content of the semaphore pairing at the same time
        final CyclicBarrier iteration = new CyclicBarrier(TASK_COUNT, () -> System.out.println("Iteration starts ..."));

        // Exchanging the greetings
        final Exchanger<String> exchanger = new Exchanger<>();

        final Collection<Runnable> tasks = new ArrayList<>();
        for (int t = 1; t <= TASK_COUNT; t++) {
            final int taskId = t; // Save the ID for printing

            final Runnable task = () -> {
                System.out.println("Starting task #" + taskId);

                try {
                    // Let's spread the threads to see the barrier effect
                    Thread.sleep(TimeUnit.SECONDS.toMillis(taskId));

                    for (int i = 0; i < ITERATION_COUNT; i++) {
                        final int index = iteration.await(); // Gather all threads
                        System.out.format("Task #%d won index %d%n", taskId, index);

                        pairing.acquire();
                        try { // Enter to the pairing area
                            System.out.format("[%s] #%d entering%n", Instant.now(), taskId);
                            final String sending = String.format("Hello! I'm task #%d", taskId);
                            final String received = exchanger.exchange(sending, taskId, TimeUnit.SECONDS);
                            System.out.format("Task #%d hears: %s%n", taskId, received);
                            // Threads met, let them sleep together for a while
                            Thread.sleep(TimeUnit.SECONDS.toMillis(index));
                            System.out.format("[%s] #%d leaving%n", Instant.now(), taskId);
                        } catch (TimeoutException e) {
                            System.out.format("Nobody waiting for #%d :o(%n", taskId);
                        } finally {
                            pairing.release();
                        }
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    System.err.println("Interrupted!");
                } catch (BrokenBarrierException e) {
                    System.err.println("Barrier got broken!");
                }
            };

            tasks.add(task);
        }

        new TaskRunner(tasks).run();
    }

    /**
     * A task runner that lets the tasks run in parallel; all tasks are forced
     * to start at the same time (each in a separate thread) and runner blocks
     * the current thread until all tasks are finished.
     */
    private static final class TaskRunner implements Runnable {

        /** Read-only collection of the tasks to run. */
        private final Collection<Runnable> tasks;
        /** Latch for ensuring a mass start of the tasks. */
        private final CountDownLatch startGate = new CountDownLatch(1);
        /** Latch for waiting for the end of all tasks. */
        private final CountDownLatch finishGate;

        /**
         * Create a new task runner.
         *
         * @param runnables
         *            the collection of tasks to run; it must a non-empty
         *            collection containing valid objects
         */
        public TaskRunner(Collection<? extends Runnable> runnables) {
            tasks = runnables.stream().peek(Objects::requireNonNull).collect(Collectors.toList());
            finishGate = new CountDownLatch(tasks.size());
        }

        /**
         * Run the tasks of the task runner.
         *
         * @see java.lang.Runnable#run()
         */
        public void run() {
            System.out.println("Starting the threads ...");
            final Instant start = Instant.now();

            try {
                // Start the tasks in new threads; we need a thread per a task
                // in this case, hence we use Thread class, otherwise using an
                // ExecutorService which creates new threads always could be
                // also an option
                for (final Runnable task : tasks) {
                    new Thread(() -> {
                        try {
                            startGate.await();
                            try {
                                task.run();
                            } finally {
                                finishGate.countDown();
                            }
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            System.err.println("Interrupted!");
                        }
                    }).start();
                }
            } finally {
                // Unblock waiting thread; run it in finally to be sure that
                // the threads are unblocked always
                startGate.countDown();
            }

            try { // Wait for the threads to finish
                finishGate.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("Interrupted!");
            }

            System.out.format("Tasks finished after %s.", start.until(Instant.now(), ChronoUnit.SECONDS));
        }
    }
}
