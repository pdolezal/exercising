package net.yetamine.lectures.platform.concurrent.pipe;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * A bounded queue implementation with intrinsic locks.
 *
 * @param <E>
 *            the type of the elements
 */
public final class BoundedIntrinsicPipe<E> implements BoundedPipe<E> {

    /**
     * Underlying thread-unsafe queue.
     *
     * <p>
     * This objects serves as both the lock and the condition variable with the
     * condition {@code queue.isEmpty() || (capacity <= queue.size())}.
     */
    private final Deque<E> queue = new ArrayDeque<>();

    /**
     * Capacity limit.
     *
     * <p>
     * Requires {@link #queue} as the lock, appears in waiting conditions for
     * the queue being full. Technically, it might be zero or even negative,
     * which would block both consumers and suppliers, but it is a bit weird,
     * therefore we rather forbid such values.
     */
    private int capacity;

    /**
     * Creates a new instance.
     *
     * @param maximalCapacity
     *            the capacity limit to set. It must be positive.
     */
    public BoundedIntrinsicPipe(int maximalCapacity) {
        capacity = checkCapacity(maximalCapacity);
    }

    /**
     * @see net.yetamine.lectures.platform.concurrent.pipe.Pipe#take()
     */
    public E take() throws InterruptedException {
        final E result;
        synchronized (queue) {
            while (queue.isEmpty()) {
                queue.wait();
            }

            result = queue.removeLast();
            if (queue.size() < capacity) {
                queue.notifyAll();
            }
        }

        return result;
    }

    /**
     * @see net.yetamine.lectures.platform.concurrent.pipe.Pipe#poll()
     */
    public Optional<E> poll() {
        final E result;
        synchronized (queue) {
            // Note: here we rely on null being forbidden; for a null-friendly solution
            // we would have to apply first this test (and then the existing code):
            //
            // if (queue.isEmpty()) {
            //     return Optional.empty();
            // }
            //
            result = queue.pollLast();
            if (queue.size() < capacity) {
                queue.notifyAll();
            }
        }

        return Optional.ofNullable(result);
    }

    /**
     * @see net.yetamine.lectures.platform.concurrent.pipe.Pipe#put(java.lang.Object)
     */
    public void put(E element) throws InterruptedException {
        Objects.requireNonNull(element);
        synchronized (queue) {
            while (capacity <= queue.size()) {
                queue.wait();
            }

            enqueue(element);
        }
    }

    /**
     * @see net.yetamine.lectures.platform.concurrent.pipe.Pipe#put(java.lang.Object,
     *      long, java.util.concurrent.TimeUnit)
     */
    public void put(E element, long timeout, TimeUnit unit) throws InterruptedException, TimeoutException {
        Objects.requireNonNull(element);

        long remaining = unit.toNanos(timeout); // Implicit null check
        // Record the current time for following deadline computation
        final long origin = System.nanoTime();

        synchronized (queue) {
            while (capacity <= queue.size()) {
                if (remaining <= 0) { // The timeout elapsed!
                    throw new TimeoutException();
                }

                TimeUnit.NANOSECONDS.timedWait(queue, remaining); // Works like queue.wait(...), but takes care of the nanosecond fraction

                // Unfortunately, waiting may finish spuriously, so we have to compute
                // how much time remains to wait in a retry
                remaining -= System.nanoTime() - origin;
            }

            enqueue(element);
        }
    }

    /**
     * @see net.yetamine.lectures.platform.concurrent.pipe.BoundedPipe#capacity()
     */
    public int capacity() {
        synchronized (queue) {
            return capacity;
        }
    }

    /**
     * @see net.yetamine.lectures.platform.concurrent.pipe.BoundedPipe#capacity(int)
     */
    public void capacity(int value) {
        checkCapacity(value);
        synchronized (queue) {
            if (capacity < value) {
                queue.notifyAll();
            }

            capacity = value;
        }
    }

    /**
     * Enqueues the given element.
     *
     * <p>
     * This method requires {@link #queue} to be held as the lock by the current
     * thread and relies on having enough capacity in the queue for the element
     * to be added.
     *
     * @param element
     *            the element to be enqueued
     */
    private void enqueue(E element) {
        assert Thread.holdsLock(queue);
        assert (queue.size() < capacity);

        final boolean wasEmpty = queue.isEmpty();
        queue.addFirst(element);
        if (wasEmpty) {
            queue.notifyAll(); // notify() does not work due to non-uniform waiters
        }
    }

    /**
     * Checks the desired capacity.
     *
     * @param value
     *            the value to check. It must be positive.
     *
     * @return the value to check
     */
    private static int checkCapacity(int value) {
        if (value <= 0) {
            throw new IllegalArgumentException("Desired capacity must be a positive number.");
        }

        return value;
    }
}
