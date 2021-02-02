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
        // 注解解析器
    }

}

class User {
    int age = 0;
}
