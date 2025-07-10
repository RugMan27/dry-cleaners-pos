package xyz.rugman27.drycleanerspos.utilites;

public class ThreadUtil {

    public static void runDelayed(Runnable task, long delayMillis) {
        new Thread(() -> {
            try {
                Thread.sleep(delayMillis);
                task.run();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("Thread was interrupted.");
            }
        }).start();
    }
}
