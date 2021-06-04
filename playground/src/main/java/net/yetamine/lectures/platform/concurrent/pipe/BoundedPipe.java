package net.yetamine.lectures.platform.concurrent.pipe;

/**
 * A thread-safe queue with a limited interface.
 *
 * @param <E>
 *            the type of the elements
 */
public interface BoundedPipe<E> extends Pipe<E> {

    /**
     * Returns the current capacity of the queue.
     *
     * @return the current capacity
     */
    int capacity();

    /**
     * Sets the new capacity of the queue.
     *
     * <p>
     * This method just sets the capacity limit, setting a value lower than the
     * current occupancy does not discard any elements.
     *
     * @param value
     *            the capacity limit to set. It must be positive.
     */
    void capacity(int value);
}
