package test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class Main {

    private static final int MAX_USER = 100;

    private static final int MAX_TASK = 1000;

    public static void main(String[] args) throws InterruptedException {
        List<User> userList = createUserList();
        noLockTask(userList);
        lockTask(userList);
    }

    private static void lockTask(List<User> users) throws InterruptedException {
        List<Thread> threadList = new ArrayList<>(MAX_USER * MAX_TASK);
        CountDownLatch count = new CountDownLatch(MAX_USER * MAX_TASK);
        for (int i = 0; i < MAX_USER; i++) {
            for (int j = 0; j < MAX_TASK; j++) {
                threadList.add(new Thread(new LockTask(users.get(i), count)));
            }
        }

        long start = System.currentTimeMillis();
        for (int i = 0; i < MAX_USER * MAX_TASK; i++) {
            threadList.get(i).start();
        }
        count.await();
        System.out.println("lockTask用时：" + (System.currentTimeMillis() - start) + " ms");
        users.forEach(System.out::println);
    }

    private static void noLockTask(List<User> users) throws InterruptedException {
        List<Thread> threadList = new ArrayList<>(MAX_USER);
        CountDownLatch count = new CountDownLatch(MAX_USER);
        for (int i = 0; i < MAX_USER; i++) {
            List<NoLockTask> userTaskList = new ArrayList<>(MAX_TASK);
            threadList.add(new Thread(new NoLockTask(userTaskList, count)));
            for (int j = 0; j < MAX_TASK; j++) {
                userTaskList.add(new NoLockTask(users.get(i)));
            }
        }

        long start = System.currentTimeMillis();
        for (int i = 0; i < MAX_USER; i++) {
            threadList.get(i).start();
        }
        count.await();
        System.out.println("noLockTask用时：" + (System.currentTimeMillis() - start) + " ms");
        users.forEach(System.out::println);
    }

    private static List<User> createUserList() {
        List<User> users = new ArrayList<>(MAX_USER);
        for (int i = 0; i < MAX_USER; i++) {
            users.add(new User(i + 1, 0));
        }
        return users;
    }
}
