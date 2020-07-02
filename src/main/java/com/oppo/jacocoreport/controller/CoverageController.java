package com.oppo.jacocoreport.controller;

import com.oppo.jacocoreport.coverage.ReportGeneratorCov;
import com.oppo.jacocoreport.coverage.entity.ApplicationCodeInfo;
import com.oppo.jacocoreport.coverage.entity.Data;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CoverageController {

    //@GetMapping("/startcoveragetask")
    @PostMapping("/startcoveragetask")
    public Data startcoveragetask(@RequestBody ApplicationCodeInfo applicationCodeInfo){
        return dealWith(applicationCodeInfo);
    }

    private Data dealWith(ApplicationCodeInfo applicationCodeInfo){
         String gitPath = applicationCodeInfo.getGitPath();
         String testedBranch = applicationCodeInfo.getTestedBranch();
         String basicBranch = applicationCodeInfo.getBasicBranch();
         String versionname = applicationCodeInfo.getVersionName();
         if(StringUtils.isEmpty(gitPath)){
             return new Data().setCode(-1).setData("gitpath can not be blank");
         }
        if(StringUtils.isEmpty(testedBranch)){
            return new Data().setCode(-2).setData("testedBranch can not be blank");
        }
        if(StringUtils.isEmpty(basicBranch)){
            return new Data().setCode(-3).setData("basicBranch can not be blank");
        }
        if(StringUtils.isEmpty(versionname)){
            return new Data().setCode(-4).setData("environment can not be blank");
        }
        ReportGeneratorCov reportGeneratorCov = new ReportGeneratorCov(gitPath,versionname,testedBranch,basicBranch,"","");
        reportGeneratorCov.startCoverageTask();
         return new Data().setCode(200).setData(gitPath);
    }
}
