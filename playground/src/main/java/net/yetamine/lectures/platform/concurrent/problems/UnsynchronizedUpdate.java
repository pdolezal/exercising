package net.yetamine.lectures.platform.concurrent.problems;

/**
 * What could be the output of this program?
 */
public final class UnsynchronizedUpdate {

    private Integer x;
    private Integer y;

    public void set(int a, int b) {
        x = a;
        y = b;
    }

    public int sum() {
        return (y != null) ? x + y : 0;
    }

    public static void main(String... args) {
        final UnsynchronizedUpdate instance = new UnsynchronizedUpdate();
        new Thread(() -> instance.set(1024, 2048)).start(); // Thread A
        new Thread(() -> System.out.println(instance.sum())).start(); // Thread B
    }
}
