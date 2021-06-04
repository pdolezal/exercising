package net.yetamine.lectures.platform.concurrent;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

/**
 * Demonstrates some operations on {@link CompletableFuture} and their effects.
 */
public final class CompletingFuture {

    public static void main(String... args) {
        final CompletableFuture<Integer> cf = new CompletableFuture<>();
        System.out.println("Starting...");

        final var pipeline = cf.thenApply(i -> {
            System.out.format("[%s] A#1: %d%n", Thread.currentThread().getName(), i);
            nap(Duration.ofSeconds(1));
            return i;
        }).thenApplyAsync(i -> { // Triggers asynchronous processing
            final int result = -i;
            System.out.format("[%s] A#2: %d%n", Thread.currentThread().getName(), result);
            return result;
        }).thenAccept(i -> System.out.format("[%s] A#3: %d%n", Thread.currentThread().getName(), i));

        IntStream.range(0, 4).forEach(j -> {
            cf.thenAccept(i -> {
                nap(Duration.ofSeconds(1));
                System.out.format("[%s] B#%d: %d%n", Thread.currentThread().getName(), j, i);
            });
        });

        System.out.println("Completing...");
        cf.complete(1);
        cf.completeExceptionally(new RuntimeException("Ooops")); // No effect when completed already
        System.out.println("Joining...");
        pipeline.join();
    }

    private static void nap(Duration duration) {
        try {
            TimeUnit.NANOSECONDS.sleep(duration.toNanos());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
