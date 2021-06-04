package net.yetamine.lectures.platform.concurrent.spinlock;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.lang.invoke.VarHandle.AccessMode;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

/**
 * Minimalistic reentrant implementation of a spin lock (without support for
 * {@link Condition}).
 */
public final class ReentrantSpinLock implements Lock {

    /**
     * Handle to {@link #owner} providing {@link AtomicReference}-like access.
     */
    private static final VarHandle OWNER;
    static {
        try {
            OWNER = MethodHandles.lookup().findVarHandle(ReentrantSpinLock.class, "owner", Thread.class);
            assert OWNER.isAccessModeSupported(AccessMode.COMPARE_AND_EXCHANGE);
            assert OWNER.isAccessModeSupported(AccessMode.GET_VOLATILE);
            assert OWNER.isAccessModeSupported(AccessMode.SET_VOLATILE);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new AssertionError(e); // May never happen here
        }
    }

    /* Implementation notes:
     * This class is also an example of piggybacking/confinement technique. The
     * acquisition counter is consistent thanks to the the atomic variable
     * access (the lock provides synchronization to itself).
     */

    /**
     * Maximal {@link #acquired} value possible to be able to recognize when
     * lock is released again.
     */
    private static final int ACQUISITION_LIMIT = -1; // Start of the counter - 1

    /** {@link AtomicReference} analogue. */
    private volatile Thread owner;
    /** Acquisition counter. */
    private int acquired;

    /**
     * Creates a new instance.
     */
    public ReentrantSpinLock() {
        // Default constructor
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        // Well, take the best effort, this is informative only... or we could make the field
        // volatile (which has no effect on VarHandle::compareAndSet) and have it a bit more
        // reliable here
        return String.format("ReentrantSpinLock[id=%d, owner=%s]", System.identityHashCode(this), owner);
    }

    /**
     * @see java.util.concurrent.locks.Lock#lock()
     */
    public void lock() {
        while (!tryLock()) {
            Thread.onSpinWait();
        }
    }

    /**
     * @see java.util.concurrent.locks.Lock#lockInterruptibly()
     */
    public void lockInterruptibly() throws InterruptedException {
        while (!tryLock()) {
            if (Thread.currentThread().isInterrupted()) {
                throw new InterruptedException();
            }

            Thread.onSpinWait();
        }
    }

    /**
     * @see java.util.concurrent.locks.Lock#newCondition()
     */
    public Condition newCondition() {
        throw new UnsupportedOperationException(); // Somewhat too complicated for a simple spin lock
    }

    /**
     * @see java.util.concurrent.locks.Lock#tryLock()
     */
    public boolean tryLock() {
        final Thread caller = Thread.currentThread();
        final Thread locked = (Thread) OWNER.compareAndExchange(this, null, caller);

        if (locked == caller) {
            // The owner is the current thread, hence accessing 'acquired' is a
            // local operation and no worry about memory visibility
            if (acquired == ACQUISITION_LIMIT) {
                throw new IllegalStateException(String.format("Acquisition limit reached for %s. Lock broken.", this));
            }

            ++acquired;
            return true;
        } else if (locked == null) {
            // Lock acquired by the new owner. Atomic variable access ensures
            // correct visibility of 'acquired', hence it has the up-to-date
            // value (should be zero).
            assert (acquired == 0);

            // Note that writing to the variable is open. No other thread should
            // access this variable!
            acquired = 1;

            return true;
        }

        return false; // Acquisition attempt failed
    }

    /**
     * @see java.util.concurrent.locks.Lock#tryLock(long,
     *      java.util.concurrent.TimeUnit)
     */
    public boolean tryLock(long value, TimeUnit unit) throws InterruptedException {
        final long timeout = unit.toNanos(value);
        final long timestamp = System.nanoTime();

        while (!tryLock()) {
            if (Thread.currentThread().isInterrupted()) {
                throw new InterruptedException();
            }

            if (timeout < System.nanoTime() - timestamp) {
                return false;
            }

            Thread.onSpinWait();
        }

        return true;
    }

    /**
     * @see java.util.concurrent.locks.Lock#unlock()
     */
    public void unlock() {
        final Thread caller = Thread.currentThread();

        if (owner == caller) { // Here using volatile declaration, otherwise OWNER (VarHandle) would have to be used
            // The owner is the current thread, hence accessing 'acquired' is a
            // local operation and no worry about memory visibility
            assert (acquired != 0);

            if (--acquired == 0) {
                // Releasing the lock when the counter dropped back to zero.
                // Note that the access to the atomic variable causes the value
                // to be visible to any thread that accesses the atomic variable
                // too. See the implementation of tryLock().
                owner = null;
            }

            return;
        }

        final String f = "Thread '%s' tried to unlock %s, but didn't own it.";
        throw new IllegalStateException(String.format(f, caller, this));
    }
}
