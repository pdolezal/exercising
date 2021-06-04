package net.yetamine.lectures.platform.concurrent;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

/**
 * Shows how to use an {@link ExecutorService} and how to control its
 * termination.
 */
public final class UsingExecutorService {

    public static void main(String... args) {
        // Using a thread pool instead of plain threads
        final ExecutorService pool = Executors.newCachedThreadPool();

        IntStream.range(1, 5).forEach(identifier -> {
            pool.submit(() -> {
                while (true) {
                    System.out.format("%d: Not yet...%n", identifier);

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }

                System.out.format("%d: Finishing...%n", identifier);
            });
        });

        try {
            for (int i = 0; i < 10; i++) {
                System.out.println("Waiting...");
                Thread.sleep(1000);
            }

            System.out.println("Finishing!");
            pool.shutdownNow(); // Request the pool to shutdown idle threads and not to accept new tasks
            System.out.println("Joining...");
            pool.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS); // Wait for all tasks to finish
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
