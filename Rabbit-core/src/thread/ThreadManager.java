package thread;

import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 线程管理器
 *
 * @author zhuchuanji
 */
public final class ThreadManager {

    protected static int corePoolSize = 200;
    protected static int maximumPoolSize = 1000;
    protected static long keepAliveTime = 60;
    protected static int maxUser;
    protected static int aliveTime;
    protected static TimeUnit unit = TimeUnit.SECONDS;
    protected static RejectedExecutionHandler handler = new ThreadPoolExecutor.AbortPolicy();
    public static final UserThreadPoolExecutor USER;
    public static ThreadPoolExecutor SYSTEM;

    static {
        SYSTEM = new ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, unit, new LinkedTransferQueue<>(),
                createThreadFactory("system", false), handler);
        USER = new UserThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, unit, new LinkedTransferQueue<>(),
                createThreadFactory("user", false), handler);
    }

    /**
     * 创建线程工厂
     *
     * @param executorName 线程归属名称
     * @param daemon       是否为守护线程
     * @return threadFactory
     */
    private static ThreadFactory createThreadFactory(String executorName, boolean daemon) {
        return r -> new Worker(executorName, daemon, r);
    }

    /**
     * 关闭线程管理器，并且关闭相关的所有线程池
     */
    public static void shutdown() {
        USER.shutdown();
        SYSTEM.shutdown();
    }

    /**
     * 封装线程
     */
    private static class Worker extends Thread {

        private Worker(String executorName, boolean daemon, Runnable runnable) {
            super(runnable);
            this.setDaemon(daemon);
            this.setName(executorName + "." + this.getId());
        }

        @Override
        public void run() {
            try {
                super.run();
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }

    }

}
