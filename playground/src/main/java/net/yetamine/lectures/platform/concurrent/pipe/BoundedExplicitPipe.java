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
 * A bounded queue implementation with explicit locks.
 *
 * @param <E>
 *            the type of the elements
 */
public final class BoundedExplicitPipe<E> implements BoundedPipe<E> {

    /**
     * Underlying thread-unsafe queue.
     *
     * <p>
     * Accessing this queue required holding {@link #lock}.
     */
    private final Deque<E> queue = new LinkedList<>();

    /** Lock to protect {@link #queue}. */
    private final Lock lock = new ReentrantLock(true);
    // Using a fair lock makes the tests better-looking as it keeps indeed the locking order

    /** Condition with {@link #lock} for {@link #queue} being non-empty. */
    private final Condition nonEmpty = lock.newCondition();

    /**
     * Condition with {@link #lock} for {@link #queue} being not full
     * already/yet {@link #capacity} threshold.
     */
    private final Condition nonFull = lock.newCondition();

    /** Capacity limit. */
    private int capacity;

    /**
     * Creates a new instance.
     *
     * @param maximalCapacity
     *            the capacity limit to set. It must be positive.
     */
    public BoundedExplicitPipe(int maximalCapacity) {
        capacity = checkCapacity(maximalCapacity);
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
        final E result;
        lock.lock();
        try {
            while (queue.isEmpty()) {
                nonEmpty.await();
            }

            result = queue.removeLast();
            if (queue.size() < capacity) {
                nonFull.signal();
            }
        } finally {
            lock.unlock();
        }

        return result;
    }

    /**
     * @see net.yetamine.lectures.platform.concurrent.pipe.Pipe#poll()
     */
    public Optional<E> poll() {
        final E result;
        lock.lock();
        try {
            // Note: here we rely on null being forbidden; for a null-friendly solution
            // we would have to apply first this test (and then the existing code):
            //
            // if (queue.isEmpty()) {
            //     return Optional.empty();
            // }
            //
            result = queue.pollLast();
            if (queue.size() < capacity) {
                nonFull.signal();
            }
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
            while (capacity <= queue.size()) {
                nonFull.await();
            }

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
        Objects.requireNonNull(element);
        long remaining = unit.toNanos(timeout);
        lock.lock();
        try {
            while (capacity <= queue.size()) {
                if (remaining <= 0) {
                    throw new TimeoutException();
                }

                remaining = nonFull.awaitNanos(remaining);
            }

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
     * @see net.yetamine.lectures.platform.concurrent.pipe.BoundedPipe#capacity()
     */
    public int capacity() {
        lock.lock();
        try {
            return capacity;
        } finally {
            lock.unlock();
        }
    }

    /**
     * @see net.yetamine.lectures.platform.concurrent.pipe.BoundedPipe#capacity(int)
     */
    public void capacity(int value) {
        checkCapacity(value);
        lock.lock();
        try {
            if (capacity < value) {
                nonFull.signalAll();
            }

            capacity = value;
        } finally {
            lock.unlock();
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
