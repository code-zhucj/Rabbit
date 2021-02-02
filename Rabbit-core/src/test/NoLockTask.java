package test;

import java.util.List;
import java.util.concurrent.CountDownLatch;

public class NoLockTask implements Runnable {

    private User user;

    public NoLockTask(User user) {
        this.user = user;
    }

    @Override
    public void run() {
        Main.sleep();
        this.user.count++;
    }

}
