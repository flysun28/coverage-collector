package com.oppo.jacocoreport.controller;

import com.oppo.jacocoreport.component.AsyncTask;
import com.oppo.jacocoreport.coverage.ReportGeneratorCov;
import com.oppo.jacocoreport.coverage.entity.ApplicationCodeInfo;
import com.oppo.jacocoreport.coverage.entity.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Timer;

@RestController
public class CoverageController {

    @Autowired
    private AsyncTask asyncTask;

    //@GetMapping("/startcoveragetask")
    @PostMapping("/startcoveragetask")
    public Data startcoveragetask(@RequestBody ApplicationCodeInfo applicationCodeInfo){
        return dealWith(applicationCodeInfo);
    }
    @GetMapping("/stopcoveragetask")
    public Data stopcoveragetask(@RequestParam(name="taskID") long taskID){
        try {
            Map<String, Timer> timerMap = ReportGeneratorCov.getTimerMap();
            if(timerMap.containsKey(String.valueOf(taskID))) {
                timerMap.get(String.valueOf(taskID)).cancel();
                timerMap.remove(String.valueOf(taskID));
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return new Data().setCode(200).setData("success");
    }

    private Data dealWith(ApplicationCodeInfo applicationCodeInfo){
         String gitPath = applicationCodeInfo.getGitPath();
         String testedBranch = applicationCodeInfo.getTestedBranch();
         String versionname = applicationCodeInfo.getVersionName();
         if(StringUtils.isEmpty(gitPath)){
             return new Data().setCode(-1).setData("gitpath can not be blank");
         }
        if(StringUtils.isEmpty(testedBranch)){
            return new Data().setCode(-2).setData("testedBranch can not be blank");
        }
        if(StringUtils.isEmpty(versionname)){
            return new Data().setCode(-4).setData("versionname can not be blank");
        }
        //异步执行覆盖率任务
        asyncTask.startCoverageTask(applicationCodeInfo);
         return new Data().setCode(200).setData("success");
    }


}
