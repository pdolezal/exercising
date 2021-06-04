package net.yetamine.lectures.platform.concurrent;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Demonstrates how to use {@link CompletionService}.
 */
public final class UsingCompletionService {

    /** The data for this demo. */
    private static final List<Integer> DATA = Arrays.asList(5, 4, 3, 2, 6);

    public static void main(String... args) {
        final ExecutorService executor = Executors.newCachedThreadPool();
        final CompletionService<Integer> service = new ExecutorCompletionService<>(executor);

        System.out.println("Submitting tasks ...");
        DATA.forEach(input -> service.submit(() -> {
            System.out.format("Processing '%s' ... %n", input);
            Thread.sleep(TimeUnit.SECONDS.toMillis(input));
            return -input;
        }));

        System.out.println("Tasks submitted ... waiting for results");

        try {
            for (int i = DATA.size(); i > 0; i--) {
                final Future<Integer> result = service.take();
                try { // Print the result
                    System.out.println("Result: " + result.get());
                } catch (ExecutionException e) {
                    System.err.println("Execution failed: " + e);
                }
            }

            System.out.println("Done!");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Interruption requested ... terminating!");
        } finally {
            executor.shutdownNow();
        }
    }
}
