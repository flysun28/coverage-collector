package com.oppo.jacocoreport.controller;

import com.oppo.jacocoreport.component.AsyncTask;
import com.oppo.jacocoreport.coverage.ReportGeneratorCov;
import com.oppo.jacocoreport.coverage.entity.ApplicationCodeInfo;
import com.oppo.jacocoreport.coverage.entity.CoverageData;
import com.oppo.jacocoreport.coverage.entity.Data;
import com.oppo.jacocoreport.coverage.utils.Jsouphtml;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.io.File;

@RestController
public class CoverageController {

    @Autowired
    private AsyncTask asyncTask;

    //@GetMapping("/startcoveragetask")
    @PostMapping("/startcoveragetask")
    public Data startcoveragetask(@RequestBody ApplicationCodeInfo applicationCodeInfo){
        return dealWith(applicationCodeInfo);
    }

    private Data dealWith(ApplicationCodeInfo applicationCodeInfo){
         String taskId = applicationCodeInfo.getId().toString();
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
        System.out.println(applicationCodeInfo.toString());
        asyncTask.startCoverageTask(applicationCodeInfo);
        System.out.println(gitPath);
         return new Data().setCode(200).setData("success");
    }


}
