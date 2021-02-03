package thread;

import module.Module;
import util.CollectionUtils;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 用户线程管理器
 *
 * @author zhuchuanji
 * @date 2021/1/27
 */
public final class UserThreadManager implements UserExecutor, Module {

    private final int maxUser;

    private final int corePoolSize;

    private final static long aliveTime = 0;

    private final ThreadPoolExecutor threadPoolExecutor;

    private Map<Object, ThreadTask> usersTasks;

    private CheckTask checkTask;

    public UserThreadManager(ThreadPoolExecutor threadPoolExecutor, int maxUser, int corePoolSize) {
        this.threadPoolExecutor = threadPoolExecutor;
        this.maxUser = maxUser;
        this.corePoolSize = corePoolSize;
    }


    @Override
    public <I, R> void submit(I id, UserFutureTask<R> userTask) {
        ThreadTask threadTask = usersTasks.computeIfAbsent(id, v -> this.newThreadTask());
        threadTask.offer(userTask);
    }

    @Override
    public void init() {
        checkTask = new CheckTask();
        usersTasks = new ConcurrentHashMap<>((int) (maxUser / 0.75F) + 1, 0.75F, corePoolSize);
    }

    @Override
    public void process() {
        threadPoolExecutor.execute(checkTask);
    }

    @Override
    public void destroy() {
        shutdown();
    }

    private ThreadTask newThreadTask() {
        return new ThreadTask(new LinkedBlockingQueue<>(), false, System.currentTimeMillis());
    }

    public void shutdown() {
        this.checkTask.close();
        threadPoolExecutor.shutdown();
        while (true) {
            if (threadPoolExecutor.isTerminated()) {
                break;
            }
        }
    }

    private class CheckTask implements Runnable {

        private volatile boolean running = true;

        @Override
        public void run() {
            while (isRunning() || CollectionUtils.isNotEmpty(usersTasks)) {
                Iterator<ThreadTask> iterator = usersTasks.values().iterator();
                while (iterator.hasNext()) {
                    ThreadTask threadTask = iterator.next();
                    if (!threadTask.isOccupationStatus()) {
                        if (threadTask.queue.size() > 0) {
                            threadTask.setOccupationStatus(true);
                            threadPoolExecutor.execute(threadTask);
                        } else {
                            threadTask.remove(iterator);
                        }
                    }
                }
            }
        }

        private boolean isRunning() {
            return running;
        }

        private synchronized void close() {
            this.running = false;
        }
    }


    private static class ThreadTask implements Runnable {

        private final BlockingQueue<UserFutureTask<?>> queue;

        private boolean occupationStatus;

        private long time;

        private ReentrantLock lock = new ReentrantLock();

        private ThreadTask(BlockingQueue<UserFutureTask<?>> queue, boolean occupationStatus, long time) {
            this.queue = queue;
            this.occupationStatus = occupationStatus;
            this.time = time;
        }

        public synchronized boolean isOccupationStatus() {
            return occupationStatus;
        }

        public void setOccupationStatus(boolean occupationStatus) {
            this.occupationStatus = occupationStatus;
        }

        public long getTime() {
            return this.time;
        }

        public void setTime(long time) {
            this.time = time;
        }

        public void offer(UserFutureTask<?> threadTask) {
            try {
                lock.lock();
                queue.offer(threadTask);
                setTime(System.currentTimeMillis());
            } finally {
                lock.unlock();
            }
        }

        public void remove(Iterator<ThreadTask> iterator) {
            try {
                lock.lock();
                if (System.currentTimeMillis() - getTime() >= aliveTime) {
                    iterator.remove();
                }
            } finally {
                lock.unlock();
            }
        }

        @Override
        public void run() {
            process();
            setOccupationStatus(false);
        }

        private void process() {
            UserFutureTask<?> userTask;
            try {
                while ((userTask = queue.poll()) != null) {
                    userTask.run();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 用户任务
     *
     * @author zhuchuanji
     */
    private static class UserTask<V> implements UserFutureTask<V> {

        private FutureTask<V> futureTask;

        public UserTask(Callable<V> callable) {
            this.futureTask = new FutureTask<>(callable);
        }

        public UserTask(Runnable runnable) {
            this.futureTask = new FutureTask<>(runnable, null);
        }

        public UserTask(Runnable runnable, V result) {
            this.futureTask = new FutureTask<>(runnable, result);
        }

        @Override
        public void run() {
            this.futureTask.run();
        }

        @Override
        public final V get() throws ExecutionException, InterruptedException {
            return this.futureTask.get();
        }

        @Override
        public final V get(long timeout, TimeUnit unit) throws ExecutionException, InterruptedException, TimeoutException {
            return this.futureTask.get(timeout, unit);
        }

        @Override
        public final boolean cancel(boolean mayInterruptIfRunning) {
            return this.futureTask.cancel(mayInterruptIfRunning);
        }

        @Override
        public final boolean isCancelled() {
            return this.futureTask.isCancelled();
        }

        @Override
        public final boolean isDone() {
            return this.futureTask.isDone();
        }
    }

}
