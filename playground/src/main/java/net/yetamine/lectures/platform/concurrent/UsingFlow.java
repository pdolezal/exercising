package net.yetamine.lectures.platform.concurrent;

import java.time.Instant;
import java.util.concurrent.Executors;
import java.util.concurrent.Flow;
import java.util.concurrent.Flow.Subscriber;
import java.util.concurrent.Flow.Subscription;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.SubmissionPublisher;
import java.util.concurrent.TimeUnit;

/**
 * Demonstrates using {@link Flow}.
 *
 * <p>
 * The demonstration makes a publisher that sends an event each second. The
 * publisher has two subscribers, one of that ignoring every second event and
 * stopping after receiving 10 events (i.e., after 20 events published).
 */
public final class UsingFlow {

    public static void main(String... args) throws Exception {
        final ScheduledExecutorService executor = Executors.newScheduledThreadPool(3);
        final int capacity = Flow.defaultBufferSize();
        try (SubmissionPublisher<Instant> publisher = new SubmissionPublisher<>(executor, capacity, null)) {
            executor.scheduleAtFixedRate(() -> publisher.submit(Instant.now()), 0, 1, TimeUnit.SECONDS);

            // Simply dump all messages
            publisher.subscribe(new Subscriber<Instant>() {

                public void onComplete() {
                    System.out.println("Eager subscriber done.");
                }

                public void onError(Throwable t) {
                    System.out.println("Eager subscriber failed. " + t);
                }

                public void onNext(Instant message) {
                    System.out.println("Eager subscriber received: " + message);
                }

                public void onSubscribe(Subscription subscription) {
                    System.out.println("Eager subscriber registered.");
                    subscription.request(Long.MAX_VALUE);
                }
            });

            // Skip each other message
            publisher.subscribe(new Subscriber<Instant>() {

                // Subscriber contract requires strictly sequential invocations. Seems to mean
                // that happens-before should apply and therefore no need to care about having
                // the fields synchronized
                private boolean skip;

                public void onComplete() {
                    System.out.println("Leaky subscriber done.");
                }

                public void onError(Throwable t) {
                    System.out.println("Leaky subscriber failed. " + t);
                }

                public void onNext(Instant message) {
                    if (skip = !skip) {
                        return;
                    }

                    System.out.println("Leaky subscriber received: " + message);
                }

                public void onSubscribe(Subscription subscription) {
                    System.out.println("Leaky subscriber registered.");
                    subscription.request(20); // Would print just 10 values
                }
            });

            Thread.sleep(TimeUnit.SECONDS.toMillis(30));
        }

        executor.shutdown();
    }
}
