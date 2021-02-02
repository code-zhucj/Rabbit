package test;

import java.util.concurrent.CountDownLatch;

public class LockTask implements Runnable {

    private User user;

    public LockTask(User user) {
        this.user = user;
    }

    @Override
    public void run() {
        this.user.lock.lock();
        Main.sleep();
        this.user.count++;
        this.user.lock.unlock();
    }
}
