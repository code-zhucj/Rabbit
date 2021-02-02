package test;

import java.util.concurrent.CountDownLatch;

public class LockTask implements Runnable {

    private User user;

    public LockTask(User user) {
        this.user = user;
    }

    @Override
    public void run() {
        try {
            this.user.lock.lock();
            Thread.sleep(2);
            this.user.count++;
            this.user.lock.unlock();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
