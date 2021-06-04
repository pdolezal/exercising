package net.yetamine.lectures.platform.concurrent;

/**
 * Shows how to use {@link Thread}.
 */
public final class UsingThread {

    public static void main(String... args) {
        final Thread thread1 = new Thread() {

            @Override
            public void run() {
                System.out.println(Thread.currentThread() + "I'm alive");
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    System.out.println("Somebody woke me up");
                }
                System.out.println(Thread.currentThread() + "I'm done");
            }
        };

        thread1.start(); // Do not confuse with thread1.run()

        new Thread(() -> {
            for (int i = 1; i <= 7; i++) {
                System.out.println(Thread.currentThread() + ": " + i);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    System.out.println("Somebody woke me up");
                }
            }
        }).start();
    }
}
