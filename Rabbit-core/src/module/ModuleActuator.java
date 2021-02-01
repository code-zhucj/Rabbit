package module;

import thread.UserTask;
import thread.UserThreadManager;

import javax.xml.ws.RequestWrapper;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @description: 模块执行器
 * @author: zhuchuanji
 * @create: 2021-01-02 00:34
 */
public class ModuleActuator {

    public void start() {
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(9, 100, 10, TimeUnit.SECONDS, new LinkedBlockingQueue<>());
        UserThreadManager userThreadManager = new UserThreadManager(threadPoolExecutor, 100, 1000);
        userThreadManager.start();
        User user = new User();
        userThreadManager.submit(new UserTask<Object>(() -> {
            user.age++;
            System.out.println("user" + user.age);
        }) {
            @Override
            public String getUserId() {
                return "张三";
            }
        });
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // 注解解析器
    }

}

class User {
    int age = 0;
}
