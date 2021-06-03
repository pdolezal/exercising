package net.yetamine.lectures.platform.concurrent.problems;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Demonstrates the problem with a concurrent operation that may occur even in a
 * single-threaded case.
 */
public final class ConcurrentModification {

    public static void main(String... args) {
        final Set<Integer> numbers = new HashSet<>(Arrays.asList(1, 2, 3, 4, 5));

        for (Integer i : numbers) {
            if (i % 2 == 0) {
                numbers.remove(i);
            }
        }
    }
}
