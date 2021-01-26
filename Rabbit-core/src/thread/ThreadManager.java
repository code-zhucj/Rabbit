package thread;

import module.Module;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * 线程管理器
 */
public class ThreadManager implements Module {

    private ThreadPoolExecutor threadPoolExecutor;

    public static ThreadPoolExecutor createThreadPool() {
        return null;
    }

    @Override
    public void init() {
//        threadPoolExecutor = new ThreadPoolExecutor();
    }

    @Override
    public void execute() {

    }

    @Override
    public void destroy() {

    }
}
