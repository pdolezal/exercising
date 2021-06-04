package net.yetamine.lectures.platform.concurrent.pipe;

import java.util.Deque;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * An unbounded queue implementation with intrinsic locks.
 *
 * @param <E>
 *            the type of the elements
 */
public final class UnboundedIntrinsicPipe<E> implements Pipe<E> {

    /**
     * Underlying thread-unsafe queue.
     *
     * <p>
     * This objects serves as both the lock and the condition variable with the
     * condition is {@code queue.isEmpty()}.
     */
    private final Deque<E> queue = new LinkedList<>();

    /**
     * Creates a new instance.
     */
    public UnboundedIntrinsicPipe() {
        // Default constructor
    }

    /**
     * @see net.yetamine.lectures.platform.concurrent.pipe.Pipe#take()
     */
    public E take() throws InterruptedException {
        synchronized (queue) {
            while (queue.isEmpty()) {
                queue.wait();
            }

            return queue.removeLast();
        }
    }

    /**
     * @see net.yetamine.lectures.platform.concurrent.pipe.Pipe#poll()
     */
    public Optional<E> poll() {
        final E result;
        synchronized (queue) {
            result = queue.pollLast();
        }

        return Optional.ofNullable(result);
    }

    /**
     * @see net.yetamine.lectures.platform.concurrent.pipe.Pipe#put(java.lang.Object)
     */
    public void put(E element) throws InterruptedException {
        Objects.requireNonNull(element);
        synchronized (queue) {
            // Conservative implementation:
            //
            // queue.addFirst(element);
            // queue.notify();
            //
            // When brave enough, conditional notification could be done:
            final boolean wasEmpty = queue.isEmpty();
            queue.addFirst(element);
            if (wasEmpty) {
                queue.notify();
            }
        }
    }

    /**
     * @see net.yetamine.lectures.platform.concurrent.pipe.Pipe#put(java.lang.Object,
     *      long, java.util.concurrent.TimeUnit)
     */
    public void put(E element, long timeout, TimeUnit unit) throws InterruptedException, TimeoutException {
        Objects.requireNonNull(unit); // Just enforce the contract despite unused
        put(element);
    }
}
