package test;

import java.util.concurrent.CountDownLatch;

public class LockTask implements Runnable {

    private User user;

    private CountDownLatch count;

    public LockTask(User user, CountDownLatch count) {
        this.user = user;
        this.count = count;
    }

    @Override
    public void run() {
        try {
            this.user.lock.lock();
            Thread.sleep(20);
            this.user.count++;
            this.user.lock.unlock();
            this.count.countDown();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
