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
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.LockSupport;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 用戶线程池
 *
 * @author zhuchuanji
 * @date 2021/2/3
 */
public final class UserThreadPoolExecutor extends ThreadPoolExecutor implements Module {

    private int maxUser = 10000;

    private Thread shutdown;

    private Map<Object, ThreadTask> usersTasks;

    private CheckTask checkTask;

    public UserThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, ThreadFactory system, RejectedExecutionHandler handler) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, system, handler);
        start();
    }

    @Override
    public void init() {
        checkTask = new CheckTask();
        usersTasks = new ConcurrentHashMap<>((int) (maxUser / 0.75F) + 1);
    }

    @Override
    public void process() {
        execute(checkTask);
    }

    @Override
    public void start() {
        init();
        execute(checkTask);
    }

    @Override
    public void destroy() {
        shutdown = Thread.currentThread();
        checkTask.close();
        LockSupport.park(shutdown);
    }

    public UserFutureTask<?> commit(Object id, Runnable runnable) {
        UserFutureTask<?> userFutureTask = newUserFutureTask(runnable);
        ThreadTask threadTask = usersTasks.computeIfAbsent(id, v -> this.newThreadTask());
        threadTask.offer(userFutureTask);
        return userFutureTask;
    }

    private UserFutureTask<?> newUserFutureTask(Runnable runnable) {
        return new UserTask<Object>(runnable);
    }


    @Override
    public void shutdown() {
        destroy();
        super.shutdown();
    }

    private ThreadTask newThreadTask() {
        return new ThreadTask(new LinkedBlockingQueue<>(), false, System.currentTimeMillis());
    }

    private class CheckTask implements Runnable {

        private volatile boolean running = true;

        @Override
        public void run() {
            Thread checkThread = Thread.currentThread();
            checkThread.setName("CheckThread." + checkThread.getId());
            while (isRunning() || CollectionUtils.isNotEmpty(usersTasks)) {
                Iterator<ThreadTask> iterator = usersTasks.values().iterator();
                while (iterator.hasNext()) {
                    ThreadTask threadTask = iterator.next();
                    if (!threadTask.isOccupationStatus()) {
                        if (threadTask.queue.size() > 0) {
                            threadTask.setOccupationStatus(true);
                            execute(threadTask);
                        } else {
                            threadTask.remove(iterator, 20);
                        }
                    }
                }
            }
            LockSupport.unpark(shutdown);
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

        private final ReentrantLock lock = new ReentrantLock();

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
            lock.lock();
            try {
                queue.offer(threadTask);
                setTime(System.currentTimeMillis());
            } finally {
                lock.unlock();
            }
        }

        public void remove(Iterator<ThreadTask> iterator, long aliveTime) {
            lock.lock();
            try {
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
