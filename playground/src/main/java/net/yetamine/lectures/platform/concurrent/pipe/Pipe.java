package net.yetamine.lectures.platform.concurrent.pipe;

import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * A thread-safe queue with a limited interface.
 *
 * @param <E>
 *            the type of the elements
 */
public interface Pipe<E> {

    /**
     * Puts an element in the queue. This method may block if the queue has
     * limited capacity.
     *
     * @param element
     *            the element to put in the queue. It must not be {@code null}.
     *
     * @throws InterruptedException
     *             if waiting for empty space in the queue was interrupted
     */
    void put(E element) throws InterruptedException;

    /**
     * Puts an element in the queue. This method may block if the queue has
     * limited capacity, but the blocking is limited with given timeout.
     *
     * @param element
     *            the element to put in the queue. It must not be {@code null}.
     * @param timeout
     *            the timeout to wait at most. If zero or negative, the method
     *            does not block at all and fails with {@link TimeoutException}
     *            immediately if the queue can't accept the element.
     * @param unit
     *            the unit of the timeout. It must not be {@code null}.
     *
     * @throws InterruptedException
     *             if waiting for empty space in the queue was interrupted
     * @throws TimeoutException
     *             if the queue could not accept the element before the given
     *             timeout elapsed
     */
    void put(E element, long timeout, TimeUnit unit) throws InterruptedException, TimeoutException;

    /**
     * Takes an element from the queue and returns the element. This method
     * blocks when no element is present in the queue.
     *
     * @return the element taken from the queue
     *
     * @throws InterruptedException
     *             if waiting for an element to return was interrupted
     */
    E take() throws InterruptedException;

    /**
     * Takes an element from the queue if present.
     *
     * @return the element taken from the queue, or an empty {@link Optional} if
     *         no element is present currently
     */
    Optional<E> poll();

    /**
     * Takes an element from the queue. This method blocks when no element is
     * present in the queue, but the blocking is limited with given timeout.
     *
     * @param timeout
     *            the timeout to wait at most. If zero or negative, the method
     *            does not block and behaves like {@link #poll()}.
     * @param unit
     *            the unit of the timeout. It must not be {@code null}.
     *
     * @return the element taken from the queue, or an empty {@link Optional} if
     *         no element became available before the timeout elapsed
     *
     * @throws InterruptedException
     *             if waiting for an element to return was interrupted
     */
    // TODO: Exercise for the reader - uncomment and fix the implementing classes
    // Optional<E> poll(long timeout, TimeUnit unit) throws InterruptedException;
}
