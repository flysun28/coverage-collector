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
        System.out.println(applicationCodeInfo.toString());
        asyncTask.startCoverageTask(applicationCodeInfo);
         return new Data().setCode(200).setData("success");
    }


}
