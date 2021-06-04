package net.yetamine.lectures.platform.concurrent;

/**
 * Demonstrates how interruption works (and why it is important).
 */
public final class Interruption {

    public static void main(String... args) {
        final Thread background = new Thread(() -> {
            while (true) {
                System.out.println("Not yet...");

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    // If not possible to rethrow (or avoid catching),
                    // restore the interruption state and try to finish
                    // as soon as possible. Try what happens if ignoring
                    // the exception!
                    Thread.currentThread().interrupt();
                    break;
                }
            }

            System.out.println("Finishing...");
        });

        background.start();

        try {
            for (int i = 0; i < 10; i++) {
                System.out.println("Waiting...");
                Thread.sleep(1000);
            }

            System.out.println("Interrupting!");
            background.interrupt();
            System.out.println("Joining...");
            background.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
