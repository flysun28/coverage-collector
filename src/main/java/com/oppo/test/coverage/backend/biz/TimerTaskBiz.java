package com.oppo.test.coverage.backend.biz;

import com.oppo.test.coverage.backend.model.constant.ErrorEnum;
import com.oppo.test.coverage.backend.model.constant.TimerTaskStopReasonEnum;
import com.oppo.test.coverage.backend.util.SystemConfig;
import com.oppo.test.coverage.backend.util.http.HttpUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author 80264236
 * @date 2021/4/8 16:24
 */
@Service
public class TimerTaskBiz {

    private static final Logger logger = LoggerFactory.getLogger(TimerTaskBiz.class);

    @Resource
    SystemConfig systemConfig;

    @Resource
    ScheduledThreadPoolExecutor scheduledThreadPoolExecutor;

    private Map<Long, ScheduledFuture<?>> timerTaskMap = new HashMap<>(100);

    private Set<String> appCodeSet = new HashSet<>();

    /**
     * 添加轮询任务
     * */
    public void addTimerTask(ReportGenerateTask task,int timeInterval){
        ScheduledFuture<?> future = scheduledThreadPoolExecutor.schedule(task,timeInterval, TimeUnit.MILLISECONDS);
        timerTaskMap.put(task.getTaskEntity().getAppInfo().getId(),future);
        appCodeSet.add(task.getTaskEntity().getAppInfo().getApplicationID());
    }


    public void stopTimerTask(Long taskId,ErrorEnum errorEnum,String appCode){

        //remove
        timerTaskMap.get(taskId).cancel(false);
        timerTaskMap.remove(taskId);
        appCodeSet.remove(appCode);

        String url = systemConfig.getSendStopTimerTaskUrl() + taskId;

        if (errorEnum!=null){
            url = url + TimerTaskStopReasonEnum.BASE.getReasonMsg() + errorEnum.getErrorMsg();
        }

        HttpUtils.sendGet(url);
    }

    public boolean isTimerTask(Long taskId){
        return timerTaskMap.get(taskId) != null;
    }

    public boolean stillTimerTask(String appCode){
        return appCodeSet.contains(appCode);
    }



}
