package net.yetamine.lectures.platform.concurrent;

import java.util.concurrent.CompletableFuture;
import java.util.stream.IntStream;

/**
 * Demonstrates how to join multiple {@link CompletableFuture} instances.
 */
public final class JoiningFuture {

    public static void main(String... args) {
        System.out.println("Focusing to see the future...");

        // There is a fluent standalone future, CompletableFuture, that can use either
        // the given thread pool, or use the common one. It is possible to chain the
        // operations like with a stream.
        //
        // However, the API is clumsy sometimes, e.g., here arrays are required, but
        // arrays and generic types do not work well together, so unchecked warnings
        // can become inevitable.

        final CompletableFuture<?>[] futures = IntStream.range(1, 5).mapToObj(rank -> {
            final int limit = 10 << rank;
            return CompletableFuture.supplyAsync(() -> {
                long fib1 = 0;
                long fib2 = 1;
                for (int i = 2; i <= limit; i++) {
                    final long next = Math.addExact(fib1, fib2);
                    fib1 = fib2;
                    fib2 = next;
                }

                return fib2;
            }).thenAccept(value -> {
                System.out.format("Computed rank %d: %d%n", rank, value);
            }).exceptionally(e -> {
                System.err.format("Error occurred: %s%n", e.getMessage());
                return null;
            });
        }).toArray(CompletableFuture[]::new);

        CompletableFuture.allOf(futures).join(); // Making a future that will wait for all
        System.out.println("Future is clear!");
    }
}
