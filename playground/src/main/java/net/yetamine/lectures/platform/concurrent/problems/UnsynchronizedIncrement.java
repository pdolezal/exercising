package net.yetamine.lectures.platform.concurrent.problems;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

/**
 * Demonstrates the effect of processor caches.
 *
 * <p>
 * The program several threads with the identical code. Each thread reads a
 * shared value in a loop and attempts to increment it until a thread-specific
 * limit is reached. After all threads finish, the value is printed. Curiously,
 * the output may differ and sometimes may even show a value that can't happen!
 */
public final class UnsynchronizedIncrement {

    /** Count of threads to execute. */
    private static final int THREAD_COUNT = 3;

    /**
     * Minimal value when a thread stops. Set the limit low enough to be able to
     * wait for so long, but not too low as the program might reach the limit
     * before the caches are stable and code runs smoothly enough to show the
     * caching effect.
     */
    private static final int VALUE_LIMIT = 1_000_000;

    /** Shared field to increment. */
    private int shared;

    public static void main(String... args) throws Exception {
        final UnsynchronizedIncrement instance = new UnsynchronizedIncrement();
        final ExecutorService service = Executors.newFixedThreadPool(THREAD_COUNT);
        // Start the threads with different target values (given offsets from the limit)
        IntStream.range(0, THREAD_COUNT).forEach(i -> service.submit(() -> instance.task(VALUE_LIMIT + i)));
        service.shutdown();
        service.awaitTermination(Integer.MAX_VALUE, TimeUnit.NANOSECONDS);
        System.out.format("Result: %d%n", instance.shared);
    }

    /**
     * Runs the task.
     *
     * @param target
     *            the target value to increment to
     */
    private void task(int target) {
        System.out.format("Started to reach %d%n", target);

        while (shared < target) {
            ++shared;
        }

//         Comment out the code above and uncomment this piece to get
//         more proper behavior where the result is always the same:
//        while (true) {
//            synchronized (this) {
//                if (target <= shared) {
//                    break;
//                }
//
//                ++shared;
//            }
//        }

        System.out.format("Reached %d%n", target);
    }
}
