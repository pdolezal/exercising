package net.yetamine.lectures.platform.concurrent.problems;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Demonstrates a reference leaking from a constructor.
 *
 * <p>
 * Registering an instance as a callback or service provider for some clients is
 * quite common technique. However, not always executed right. It is not unusual
 * practice to register an instance before it is actually completed. While it is
 * wrong and it can cause problems in the single-threaded case as well, it can
 * become especially dangerous in the multithreaded case where the incomplete
 * instance can be accessed from another thread more unexpectedly.
 */
public final class LeakingThis {

    public static void main(String... args) throws Exception {
        // Thread pool to run the asynchronous tasks
        final ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        // Here is a board mediating some message exchange
        final MessageBoard<String> board = new DefaultMessageBoard<>(executor);
        board.publish("[system] Starting the run.");
        // Run a publisher to spill the messages
        executor.submit(() -> {
            try {
                int counter = 0;
                while (!Thread.currentThread().isInterrupted()) {
                    board.accept(Integer.toString(counter++));
                }
            } catch (RuntimeException e) {
                e.printStackTrace(System.err);
            }
        });

        // Well, even when stupid, but lucky, nothing bad happens...
        final Consumer<String> direct = new ConsolePrinter(board);
        board.publish("[common] Board test #1.");
        board.unsubscribe(direct);

        // But not all stories end well...
        board.publish("[system] Switching to filtering printer.");
        final Consumer<String> printer = new FilteringPrinter(board, s -> s.startsWith("["));
        printer.accept("[system] Filtering printer created successfully.");
        board.publish("[common] Board test #2.");
        board.publish("[system] Shutting down.");
        executor.shutdown(); // Do not wait too long
    }
}

/**
 * Message board interface for the White Board pattern mediating publishers and
 * subscribers.
 *
 * @param <M>
 *            the type of the messages
 */
interface MessageBoard<M> extends Consumer<M> {

    /**
     * Subscribes a subscriber.
     *
     * @param subscriber
     *            the subscriber to subscribe. It must not be {@code null}.
     */
    void subscribe(Consumer<? super M> subscriber);

    /**
     * Unsubscribes a subscriber.
     *
     * <p>
     * This method cancels subscription for all topics which the subscriber
     * subscribed.
     *
     * @param subscriber
     *            the subscriber to unsubscribe. Nothing happens if {@code null}
     *            is specified.
     */
    void unsubscribe(Consumer<? super M> subscriber);

    /**
     * Publishes a message to the board.
     *
     * <p>
     * The message is delivered to all subscribers. When the order of delivery
     * is sequential or parallel is completely the matter of the implementation.
     *
     * @param message
     *            the message to deliver. Generally, it should not be
     *            {@code null}, but some implementations may allow it.
     *
     * @return the future that completes when the delivery to all subscribers is
     *         finished. The future returns the message that has been published,
     *         hence it is possible to retrieve any output stored in the message
     *         by its receivers.
     */
    Future<M> publish(M message);

    /**
     * Publishes the message and waits for complete delivery.
     *
     * @throws CompletionException
     *             if the delivery fails
     *
     * @see java.util.function.Consumer#accept(java.lang.Object)
     */
    default void accept(M message) {
        try {
            publish(message).get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new CompletionException(e);
        } catch (ExecutionException e) {
            throw new CompletionException(e.getCause());
        }
    }
}

/**
 * An implementation of {@link MessageBoard} that delivers messages in parallel.
 *
 * @param <M>
 *            the type of the messages
 */
@SuppressWarnings("javadoc")
final class DefaultMessageBoard<M> implements MessageBoard<M> {

    /** Subscribers to this board. */
    private final List<Consumer<? super M>> subscribers = new CopyOnWriteArrayList<>();
    /** Executor to run delivery tasks. */
    private final Executor executor;

    /**
     * Creates a new instance.
     *
     * @param deliverer
     *            the executor to run delivery tasks. It must not be
     *            {@code null}.
     */
    public DefaultMessageBoard(Executor deliverer) {
        executor = Objects.requireNonNull(deliverer);
    }

    /**
     * Creates a new instance.
     */
    public DefaultMessageBoard() {
        this(ForkJoinPool.commonPool());
    }

    /**
     * @see net.yetamine.lectures.platform.concurrent.problems.MessageBoard#subscribe(java.util.function.Consumer)
     */
    public synchronized void subscribe(Consumer<? super M> subscriber) {
        if (!subscribers.contains(Objects.requireNonNull(subscriber))) {
            subscribers.add(subscriber);
        }
    }

    /**
     * @see net.yetamine.lectures.platform.concurrent.problems.MessageBoard#unsubscribe(java.util.function.Consumer)
     */
    public synchronized void unsubscribe(Consumer<? super M> subscriber) {
        subscribers.remove(subscriber);
    }

    /**
     * @see net.yetamine.lectures.platform.concurrent.problems.MessageBoard#publish(java.lang.Object)
     */
    public Future<M> publish(M message) {
        final CompletableFuture<Void>[] futures = subscribers.stream()                  // Make futures for all subscribers
            .map(s -> CompletableFuture.runAsync(() -> s.accept(message), executor))    // Let each of them deliver
            .toArray(CompletableFuture[]::new);

        return CompletableFuture.allOf(futures).thenApply(v -> message);
    }

    /**
     * @see net.yetamine.lectures.platform.concurrent.problems.MessageBoard#accept(java.lang.Object)
     */
    public void accept(M message) {
        subscribers.forEach(s -> s.accept(message));
    }
}

/**
 * A simple filtering printer. The implementation is immutable.
 */
@SuppressWarnings("javadoc")
final class FilteringPrinter extends ConsolePrinter {

    /**
     * Delay to simulate some work in the constructor. Set it high enough to let
     * the leaked reference being used.
     */
    private static final long SIMULATED_DELAY = 1000;

    /** Filter for the accepted messages. */
    private final Predicate<? super String> filter;

    /**
     * Creates a new instance.
     *
     * @param board
     *            the board to receive information from. It must not be
     *            {@code null}.
     * @param p
     *            the predicate for filtering. It must not be {@code null}.
     */
    public FilteringPrinter(MessageBoard<String> board, Predicate<? super String> p) {
        // This looks innocent: just another class to extend, so make the compiler
        // satisfied. The first problem might be questionable inheritance. The
        // second and more troublesome in this case, however, can't be seen at
        // this place (poor author of this class!).
        super(board);

        // Sometimes the thread executing the constructor can be interrupted and
        // the CPU granted to another. It is delayed... let's simulate it here.
        delay(SIMULATED_DELAY);

        filter = Objects.requireNonNull(p);
    }

    /**
     * @see net.yetamine.lectures.platform.concurrent.problems.ConsolePrinter#accept(java.lang.String)
     */
    @Override
    public void accept(String t) {
        // Note that the field is final, so no worries about this class not being
        // immutable or thread-safe.
        if (filter.test(t)) {
            super.accept(t);
        }
    }

    /**
     * Waits for the timeout.
     *
     * @param timeout
     *            the timeout in milliseconds
     */
    private static void delay(long timeout) {
        try {
            Thread.sleep(timeout);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

/**
 * A simple bridge printing to the console output. The implementation is
 * immutable.
 */
class ConsolePrinter implements Consumer<String> {

    // No state at all! This is completely immutable.

    /**
     * Creates a new instance.
     *
     * @param board
     *            the board to receive information from. It must not be
     *            {@code null}.
     */
    public ConsolePrinter(MessageBoard<String> board) {
        board.subscribe(this); // Oh. Here is the culprit!
    }

    /**
     * @see java.util.function.Consumer#accept(java.lang.Object)
     */
    public void accept(String t) {
        System.out.println(t);
    }
}
