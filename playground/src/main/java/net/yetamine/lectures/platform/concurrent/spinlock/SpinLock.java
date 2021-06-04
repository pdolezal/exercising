package net.yetamine.lectures.platform.concurrent.spinlock;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.lang.invoke.VarHandle.AccessMode;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

/**
 * Minimalistic non-reentrant implementation of a spin lock (without support for
 * {@link Condition}).
 */
public final class SpinLock implements Lock {

    // Exercise: Make the lock reentrant.

    /**
     * Handle to {@link #owner} providing {@link AtomicReference}-like access.
     */
    private static final VarHandle OWNER;
    static {
        try {
            OWNER = MethodHandles.lookup().findVarHandle(SpinLock.class, "owner", Thread.class);
            assert OWNER.isAccessModeSupported(AccessMode.COMPARE_AND_EXCHANGE);
            assert OWNER.isAccessModeSupported(AccessMode.GET_VOLATILE);
            assert OWNER.isAccessModeSupported(AccessMode.SET_VOLATILE);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new AssertionError(e); // May never happen here
        }
    }

    /** {@link AtomicReference} analogue. */
    private Thread owner;

    /**
     * Creates a new instance.
     */
    public SpinLock() {
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
        return String.format("SpinLock[id=%d, owner=%s]", System.identityHashCode(this), owner);
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
            if (Thread.interrupted()) {
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
        return OWNER.compareAndSet(this, null, Thread.currentThread());
    }

    /**
     * @see java.util.concurrent.locks.Lock#tryLock(long,
     *      java.util.concurrent.TimeUnit)
     */
    public boolean tryLock(long value, TimeUnit unit) throws InterruptedException {
        final long timeout = unit.toNanos(value);
        final long timestamp = System.nanoTime();

        while (!tryLock()) {
            if (Thread.interrupted()) {
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
        if (!OWNER.compareAndSet(this, caller, null)) {
            final String f = "Thread '%s' tried to unlock %s, but didn't own it.";
            throw new IllegalStateException(String.format(f, caller, this));
        }
    }
}
