package net.yetamine.lectures.platform.concurrent;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;

/**
 * Demonstrates the differences in {@link Future#cancel(boolean)} behavior.
 *
 * <p>
 * Note that {@link CompletableFuture} does not use thread interrupts. However,
 * {@link ExecutorService#shutdown()} may make the {@link CompletableFuture}
 * fail with a {@link RejectedExecutionException} (not able to launch the next
 * stage) and {@link ExecutorService#shutdownNow()} may produce interrupts
 * anyway.
 */
public final class CancellingFuture {

    public static void main(String... args) throws Exception {
        final Runnable r = () -> {
            try {
                System.out.println("Sleeping");
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.out.println("Interrupted");
            } finally {
                System.out.println("Finished");
            }
        };

        final ExecutorService executor = Executors.newSingleThreadExecutor();

        // Submit a Future and cancel it then with interruption
        final Future<?> ef = executor.submit(r);
        Thread.sleep(1000);
        ef.cancel(true);
        System.out.println("Executor::submit cancelled");
        Thread.sleep(10000);
        System.out.println("Executor::submit test finished");

        // Do the same with CompletableFuture
        final CompletableFuture<?> cf = CompletableFuture.runAsync(r, executor).handle((result, t) -> {
            System.out.format("Handled: %s | %s%n", result, t);
            return null;
        });

        Thread.sleep(1000);
        cf.cancel(true);
        System.out.println("CompletableFuture cancelled");
        Thread.sleep(10000);
        System.out.println("CompletableFuture test finished");

        executor.shutdown();
    }
}
