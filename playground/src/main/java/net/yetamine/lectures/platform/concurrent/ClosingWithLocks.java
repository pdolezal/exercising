package net.yetamine.lectures.platform.concurrent;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * An unsual way of using read-write locks.
 *
 * <p>
 * Let's assume that this class uses some resources like file handles or sockets
 * that need closing. So, it has {@link #close()} method, which may be called
 * even multiple times, but should do actual work just once. Moreover, calling
 * this method may interfere with calls of {@link #method()}, that does some
 * work: closing a socket when some data are being written could be wrong.
 * Therefore a read-write lock acts as a protection for the {@code closed} state
 * change.
 */
public final class ClosingWithLocks implements AutoCloseable {

    /** Closing lock. */
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    /** Flag indicating this instance has been closed. */
    private boolean closed;

    /**
     * Creates a new instance.
     */
    public ClosingWithLocks() {
        // Default constructor
    }

    /**
     * Does some business action, e.g., sends some data over a socket.
     */
    public void method() {
        final Lock l = lock.readLock();
        l.lock();
        try {
            if (closed) { // Do not continue if required resources were released
                throw new IllegalStateException();
            }

            // Doing something useful here, but without the risk that the required
            // resources might be closed in parallel causing various nasty I/O
            // exceptions (like ClosedChannelException)
        } finally {
            l.unlock();
        }
    }

    /**
     * Closes all resources held by the instance.
     *
     * @see java.lang.AutoCloseable#close()
     */
    public void close() {
        final Lock l = lock.writeLock();
        l.lock();
        try {
            if (closed) { // Do close once only
                return;
            }

            closed = true;
        } finally {
            l.unlock();
        }

        // Here would be the actual closing action that shall execute once only
    }
}
