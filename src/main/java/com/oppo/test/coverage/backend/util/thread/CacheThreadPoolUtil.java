package com.oppo.test.coverage.backend.util.thread;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.oppo.trace.threadpool.TraceExecutorService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.*;


/**
 * @author 80264236
 */
@Configuration
public class CacheThreadPoolUtil {


    @Bean(name = "cacheThreadPool")
    public ExecutorService threadPool() {

        ExecutorService executorService = new ThreadPoolExecutor(
                5,
                100,
                60L, TimeUnit.SECONDS,
                new SynchronousQueue<>(),
                new ThreadFactoryBuilder().setNameFormat("cache-threadPool-%d").build(),
                new MyIgnorePolicy());
        ExecutorService threadPool=new TraceExecutorService(executorService);
        return threadPool;
    }

    public static class MyIgnorePolicy implements RejectedExecutionHandler {
        @Override
        public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
            if (!executor.isShutdown()){
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException ignored) {
                }
                r.run();
            }
        }
    }



}
