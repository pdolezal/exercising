package net.yetamine.lectures.platform.concurrent;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Shows the basic use of {@link Future} interface.
 */
public final class UsingFuture {

    public static void main(String... args) {
        final ExecutorService pool = Executors.newCachedThreadPool();

        // Using a future with a thread pool to wait for a particular result

        final Future<Long> fib = pool.submit(() -> {
            long fib1 = 0;
            long fib2 = 1;
            for (int i = 2; i <= 20; i++) {
                final long next = fib1 + fib2;
                fib1 = fib2;
                fib2 = next;
            }

            return fib2;
        });

        try {
            System.out.println("Focusing to the future...");
            Thread.sleep(5000);
            System.out.println("Almost there...");
            Thread.sleep(1000);
            System.out.format("And the future is %d%n", fib.get());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (ExecutionException e) {
            System.out.println("Failed to see the future.");
        } finally {
            pool.shutdownNow();
        }
    }
}
