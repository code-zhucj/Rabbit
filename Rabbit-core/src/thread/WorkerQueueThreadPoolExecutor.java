package thread;

import module.Module;
import util.CollectionUtils;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 工作队列线程池
 *
 * <p>
 * 任意时刻同一任务队列只有一个线程在执行
 * </P>
 *
 * @author zhuchuanji
 * @date 2021/2/3
 */
public final class WorkerQueueThreadPoolExecutor extends ThreadPoolExecutor implements Module {

    private int maxUser = 10000;

    private Thread shutdown;

    private Map<Object, ThreadTask> usersTasks;

    private CheckTask checkTask;

    public WorkerQueueThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, ThreadFactory system, RejectedExecutionHandler handler) {
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

    public void commit(Object id, Runnable runnable) {
        ThreadTask threadTask = usersTasks.computeIfAbsent(id, v -> this.newThreadTask());
        threadTask.offer(runnable);
    }


    @Override
    public void shutdown() {
        destroy();
        super.shutdown();
    }

    private ThreadTask newThreadTask() {
        return new ThreadTask(new LinkedBlockingQueue<>(), false, System.currentTimeMillis());
    }

    /**
     * 检查任务，该任务在线程池启动时启动，负责检查过期的任务队列，当任务队列过期时将其移除
     */
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

    /**
     * 线程任务，负责消费任务队列中的所有任务。
     */
    private static class ThreadTask implements Runnable {

        private final BlockingQueue<Runnable> queue;

        private boolean occupationStatus;

        private long time;

        private final ReentrantLock lock = new ReentrantLock();

        private ThreadTask(BlockingQueue<Runnable> queue, boolean occupationStatus, long time) {
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

        public void offer(Runnable runnable) {
            lock.lock();
            try {
                queue.offer(runnable);
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
            Runnable r;
            try {
                while ((r = queue.poll()) != null) {
                    r.run();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
