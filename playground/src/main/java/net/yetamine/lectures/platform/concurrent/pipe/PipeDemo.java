package net.yetamine.lectures.platform.concurrent.pipe;

import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

/**
 * Tests {@link Pipe} implementations.
 */
public final class PipeDemo {

    /** Limit for sleeping for the producing threads. */
    private static final int SLEEP_LIMIT = 10;
    /** Number of producing threads. */
    private static final int PRODUCER_COUNT = 10;
    /** Count of the elements to send. */
    private static final int MESSAGE_COUNT = 10;
    /** Capacity limit (for bounded queues). */
    private static final int CAPACITY = 2;

    public static void main(String... args) throws Exception {
        final Pipe<String> pipe = new BoundedExplicitPipe<>(CAPACITY);

        final Random random = new Random();
        final ExecutorService producers = Executors.newFixedThreadPool(PRODUCER_COUNT);
        IntStream.range(0, PRODUCER_COUNT).mapToObj(taskId -> (Runnable) () -> {
            System.out.format("Started %d%n", taskId);

            try {
                for (int messageId = 0; messageId < MESSAGE_COUNT; messageId++) {
                    final String message = String.format("Message #%d from %d", messageId, taskId);
                    if (SLEEP_LIMIT > 0) { // For disabling sleeping completely
                        Thread.sleep(random.nextInt(SLEEP_LIMIT));
                    }

                    System.out.format("Sending %s%n", message);
                    pipe.put(message);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            System.out.format("Finished %d%n", taskId);
        }).forEach(producers::submit);

        final ExecutorService consumers = Executors.newSingleThreadExecutor();
        final Future<?> consumer = consumers.submit((Runnable) () -> {
            try {
                while (true) {
                    System.out.format("Received %s%n", pipe.take());
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.out.println("Requested to finish.");
            }
        });

        producers.shutdown();
        producers.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        consumer.cancel(true);
        consumers.shutdown();
    }

    private PipeDemo() {
        throw new AssertionError();
    }
}
