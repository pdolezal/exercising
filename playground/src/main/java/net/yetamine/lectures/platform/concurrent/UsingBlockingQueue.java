package net.yetamine.lectures.platform.concurrent;

import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Demonstrates using a {@link BlockingQueue} and how an executor can be
 * controlled to stop.
 */
public final class UsingBlockingQueue {

    public static void main(String... args) throws Exception {
        final BlockingQueue<Integer> queue = new LinkedBlockingQueue<>();

        final ScheduledExecutorService executor = Executors.newScheduledThreadPool(2);
        new Random().ints(5, 0, 10).peek(i -> System.out.format("Scheduling %d.%n", i)).forEach(i -> {
            executor.scheduleAtFixedRate(() -> {
                System.out.format("Offering %d.%n", i);
                queue.offer(i);
            }, i, i, TimeUnit.SECONDS);
        });

        executor.submit(() -> {
            try {
                while (true) {
                    System.out.format("Taken %d.%n", queue.take());
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.out.println("Interrupted");
            }
        });

        System.in.read(); // Waiting for ENTER to finish
        executor.shutdownNow();
    }
}
