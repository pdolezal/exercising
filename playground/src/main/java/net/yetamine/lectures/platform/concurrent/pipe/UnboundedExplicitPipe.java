package net.yetamine.lectures.platform.concurrent.pipe;

import java.util.Deque;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * An unbounded queue implementation with explicit locks.
 *
 * @param <E>
 *            the type of the elements
 */
public final class UnboundedExplicitPipe<E> implements Pipe<E> {

    /**
     * Underlying thread-unsafe queue.
     *
     * <p>
     * Accessing this queue required holding {@link #lock}.
     */
    private final Deque<E> queue = new LinkedList<>();

    /** Lock to protect {@link #queue}. */
    private final Lock lock = new ReentrantLock();
    /** Condition with {@link #lock} for {@link #queue} being non-empty. */
    private final Condition nonEmpty = lock.newCondition();

    /**
     * Creates a new instance.
     */
    public UnboundedExplicitPipe() {
        // Default constructor
    }

    /* Implementation note:
     *
     * Instead of lock.lock(), calling lock.lockInterruptibly() could be possible. It might
     * improve the responsiveness to interruptions on heavily congested locks.
     */

    /**
     * @see net.yetamine.lectures.platform.concurrent.pipe.Pipe#take()
     */
    public E take() throws InterruptedException {
        lock.lock();
        try {
            while (queue.isEmpty()) {
                nonEmpty.await();
            }

            return queue.removeLast();
        } finally {
            lock.unlock();
        }
    }

    /**
     * @see net.yetamine.lectures.platform.concurrent.pipe.Pipe#poll()
     */
    public Optional<E> poll() {
        final E result;
        lock.lock();
        try {
            result = queue.pollLast();
        } finally {
            lock.unlock();
        }

        return Optional.ofNullable(result);
    }

    /**
     * @see net.yetamine.lectures.platform.concurrent.pipe.Pipe#put(java.lang.Object)
     */
    public void put(E element) throws InterruptedException {
        Objects.requireNonNull(element);
        lock.lock();
        try {
            // Conservative implementation:
            //
            // queue.addFirst(element);
            // nonEmpty.signal();
            //
            // When brave enough, conditional notification could be done:
            final boolean wasEmpty = queue.isEmpty();
            queue.addFirst(element);
            if (wasEmpty) {
                nonEmpty.signal();
            }
        } finally {
            lock.unlock();
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
