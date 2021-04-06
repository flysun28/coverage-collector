package com.oppo.jacocoreport.controller;

import com.oppo.jacocoreport.component.AsyncTask;
import com.oppo.jacocoreport.coverage.ReportGeneratorCov;
import com.oppo.jacocoreport.coverage.entity.ApplicationCodeInfo;
import com.oppo.jacocoreport.coverage.entity.Data;
import com.oppo.jacocoreport.coverage.utils.Config;
import com.oppo.jacocoreport.coverage.utils.FolderFileScanner;
import com.oppo.jacocoreport.coverage.utils.HttpUtils;
import com.oppo.trace.agent.annotation.Tracing;
import com.oppo.trace.servlet.TraceFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Timer;

@RestController
public class CoverageController {

    @Autowired
    private AsyncTask asyncTask;

    public CoverageController(){
        super();
    }
    //@GetMapping("/startcoveragetask")

    @Bean
    public FilterRegistrationBean traceFilter() {
        FilterRegistrationBean registration = new FilterRegistrationBean();
        registration.setFilter(new TraceFilter());
        registration.addUrlPatterns("/*");
        registration.setName("traceFilter");
        registration.setOrder(1);
        return registration;
    }

    @PostMapping("/startcoveragetask")
    public Data startcoveragetask(@RequestBody ApplicationCodeInfo applicationCodeInfo){
        return dealWith(applicationCodeInfo);
    }

    @GetMapping("/stopcoveragetask")
    public Data stopcoveragetask(@RequestParam(name="taskID") long taskID,
                                 @RequestParam(name = "appCode") String appCode){
        try {
            Map<String, Timer> timerMap = ReportGeneratorCov.getTimerMap();
            if(timerMap.containsKey(String.valueOf(taskID))) {
                System.out.println(taskID);
                timerMap.get(String.valueOf(taskID)).cancel();
                timerMap.remove(String.valueOf(taskID));
                FolderFileScanner.fileUpload(appCode,taskID);
                HttpUtils.sendGet(Config.SEND_STOPTIMERTASK_URL+taskID);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return new Data().setCode(200).setData("success");
    }

    private Data dealWith(ApplicationCodeInfo applicationCodeInfo){
         if(StringUtils.isEmpty(applicationCodeInfo.getGitPath())){
             return new Data().setCode(-1).setData("gitpath can not be blank");
         }
        if(StringUtils.isEmpty(applicationCodeInfo.getTestedBranch())){
            return new Data().setCode(-2).setData("testedBranch can not be blank");
        }
        if(StringUtils.isEmpty(applicationCodeInfo.getVersionName())){
            return new Data().setCode(-3).setData("versionname can not be blank");
        }
        if(StringUtils.isEmpty(applicationCodeInfo.getTestedCommitId())){
            return new Data().setCode(-4).setData("testedCommitId can not be blank");
        }
        if(StringUtils.isEmpty(applicationCodeInfo.getBasicCommitId())){
            return new Data().setCode(-5).setData("basicCommitId can not be blank");
        }
        if(StringUtils.isEmpty(applicationCodeInfo.getBasicBranch())){
            return new Data().setCode(-6).setData("basicBranch can not be blank");
        }
        //异步执行覆盖率任务
        asyncTask.startCoverageTask(applicationCodeInfo);
         return new Data().setCode(200).setData("success");
    }


}
