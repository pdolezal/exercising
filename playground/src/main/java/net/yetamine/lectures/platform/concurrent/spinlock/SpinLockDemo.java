package net.yetamine.lectures.platform.concurrent.spinlock;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.stream.IntStream;

/**
 * Demonstrates a non-blocking technique applied on conditional atomic
 * increment.
 *
 * <p>
 * The program runs several threads with the identical code. Each thread reads a
 * shared value in a loop and attempts to increment it until a thread-specific
 * limit is reached. After all threads finish, the value is printed.
 */
public final class SpinLockDemo {

    /** Count of threads to execute. */
    private static final int THREAD_COUNT = 3;

    /**
     * Minimal value when a thread stops. Use the same limit that works with
     * {@code D01_CachingEffect} to show the caching impacts; the results should
     * be always the same in this case though thanks to the memory barriers.
     */
    private static final int VALUE_LIMIT = 1_000_000;

    /** Lock to use to access {@link #shared}. */
    private final Lock lock = new SpinLock();
    /** Shared field to increment. */
    private int shared;

    /**
     * Runs the program.
     *
     * @param args
     *            command line arguments. It must not be {@code null}.
     *
     * @throws Exception
     *             if something goes wrong
     */
    public static void main(String... args) throws Exception {
        final SpinLockDemo instance = new SpinLockDemo();
        final ExecutorService service = Executors.newFixedThreadPool(THREAD_COUNT);
        // Start the threads with different target values (given offsets from the limit)
        IntStream.range(0, THREAD_COUNT).forEach(i -> service.submit(() -> instance.run(VALUE_LIMIT + i)));
        service.shutdown();
        service.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        System.out.format("Result: %d%n", instance.shared);
    }

    /**
     * Runs the task.
     *
     * @param target
     *            the target value to increment to
     */
    private void run(int target) {
        while (true) {
            lock.lock();
            try {
                if (target <= shared) {
                    return;
                }

                ++shared;
            } finally {
                lock.unlock();
            }
        }
    }
}
