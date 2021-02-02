package test;

import thread.UserTask;
import thread.UserThreadManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;
import java.util.function.Function;
import java.util.function.Supplier;

public class Main {

    private static final int MAX_USER = 1000;

    private static final int MAX_TASK = 10000;

    public static void main(String[] args) throws InterruptedException {
        noLockTask(createUsers());
        lockTask(createUsers());
    }
    private static void noLockTask(Map<Integer, User> users) {
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(200, 1000, 10, TimeUnit.SECONDS,
                new LinkedTransferQueue<>(), v -> new Thread(v, "work"));
        UserThreadManager userThreadManager = new UserThreadManager(threadPoolExecutor, MAX_USER, MAX_TASK);
        userThreadManager.start();
        Set<UserTask<Boolean>> usersTasks = createUsersTasks(NoLockTask::new, users);
        long start = System.currentTimeMillis();
        usersTasks.forEach(userThreadManager::submit);
        userThreadManager.shotdown();
        System.out.println("noLock执行耗时： " + (System.currentTimeMillis() - start));
        print(users);
    }


    private static void lockTask(Map<Integer, User> users) {
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(200, 1000, 10, TimeUnit.SECONDS,
                new LinkedTransferQueue<>(), v -> new Thread(v, "work"));
        Set<UserTask<Boolean>> usersTasks = createUsersTasks(LockTask::new, users);
        long start = System.currentTimeMillis();
        usersTasks.forEach(threadPoolExecutor::execute);
        threadPoolExecutor.shutdown();
        while (!threadPoolExecutor.isTerminated()) {
        }
        System.out.println("lock执行耗时： " + (System.currentTimeMillis() - start));
        print(users);
    }

    private static void print(Map<Integer, User> users){
        users.values().forEach(v->{
            if(v.count != MAX_TASK){
                System.err.println(v);
            }
        });
    }

    private static Set<UserTask<Boolean>> createUsersTasks(Function<User, Runnable> function, Map<Integer, User> users) {
        Set<UserTask<Boolean>> userTasks = new HashSet<>((int) (MAX_USER * MAX_TASK / 0.75F) + 1);
        for (int i = 0; i < MAX_USER; i++) {
            int id = i + 1;
            for (int j = 0; j < MAX_TASK; j++) {
                userTasks.add(new UserTask<Boolean>(function.apply(users.get(id))) {
                    @Override
                    public String getUserId() {
                        return String.valueOf(id);
                    }
                });
            }
        }
        return userTasks;
    }

    private static Map<Integer, User> createUsers() {
        Map<Integer, User> users = new HashMap<>();
        for (int i = 0; i < MAX_USER; i++) {
            users.put(i + 1, new User(i + 1, 0));
        }
        return users;
    }

    public static void sleep() {
        long sleep = ThreadLocalRandom.current().nextLong(20);
        sleep = 2;
        try {
            Thread.sleep(sleep);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
