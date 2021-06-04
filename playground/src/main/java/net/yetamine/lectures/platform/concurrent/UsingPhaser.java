package net.yetamine.lectures.platform.concurrent;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Phaser;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

/**
 * Demonstrates how to use {@link Phaser}.
 */
public final class UsingPhaser {

    /** The number of tasks to run. */
    private static final int TASK_COUNT = 10;

    public static void main(String... args) throws Exception {
        final ExecutorService executor = Executors.newCachedThreadPool();

        // Make the phaser for starting. Let it register one party (this thread
        // for calling arriveAndDeregister later), the others will register
        // themselves as being submitted to the thread pool.
        final Phaser advance = new Phaser(1);

        IntStream.range(0, TASK_COUNT).forEach(taskId -> {
            advance.register(); // Register for the phaser

            executor.submit(() -> {
                System.out.format("Task %d waiting!%n", taskId);

                try {
                    Thread.sleep(TimeUnit.SECONDS.toMillis(taskId));
                } catch (InterruptedException e) {
                    System.out.format("Task %d interrupted!%n", taskId);
                    Thread.currentThread().interrupt();
                    return;
                }

                advance.arriveAndAwaitAdvance();
                System.out.format("Task %d advancing!%n", taskId);
            });
        });

        System.out.println("Tasks submitted.");
        advance.arriveAndDeregister(); // Decrement the awaited party count
        System.out.println("Tasks may advance.");
        executor.shutdown();
    }
}
