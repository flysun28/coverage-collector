package com.oppo.test.coverage.backend.util.thread;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.oppo.trace.threadpool.TraceScheduledExecutorService;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author 80264236
 * @date 2021/4/8 17:11
 */
@Component
public class ScheduleThreadPoolUtil {

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

    @Bean(name = "scheduledThreadPoolExecutor")
    public ScheduledThreadPoolExecutor scheduledThreadPoolExecutor(){
        ScheduledThreadPoolExecutor scheduledThreadPoolExecutor =  new ScheduledThreadPoolExecutor(20,
                new ThreadFactoryBuilder().setNameFormat("Timer-task-thread-%d").build(),
                new MyIgnorePolicy());
//        ScheduledExecutorService scheduled= new TraceScheduledExecutorService(scheduledThreadPoolExecutor);

        return scheduledThreadPoolExecutor;
    }

}
