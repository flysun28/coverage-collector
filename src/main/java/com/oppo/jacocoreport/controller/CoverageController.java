package com.oppo.jacocoreport.controller;

import com.oppo.jacocoreport.coverage.ReportGeneratorCov;
import com.oppo.jacocoreport.coverage.entity.ApplicationCodeInfo;
import com.oppo.jacocoreport.coverage.entity.CoverageData;
import com.oppo.jacocoreport.coverage.entity.Data;
import com.oppo.jacocoreport.coverage.utils.Jsouphtml;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.io.File;

@RestController
public class CoverageController {

    //@GetMapping("/startcoveragetask")
    @PostMapping("/startcoveragetask")
    public Data startcoveragetask(@RequestBody ApplicationCodeInfo applicationCodeInfo){
        return dealWith(applicationCodeInfo);
    }

    private Data dealWith(ApplicationCodeInfo applicationCodeInfo){
         String taskId = applicationCodeInfo.getId().toString();
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
        ReportGeneratorCov reportGeneratorCov = new ReportGeneratorCov(taskId,gitPath,versionname,testedBranch,basicBranch,"","");
        reportGeneratorCov.startCoverageTask();
         return new Data().setCode(200).setData("");
    }

    @GetMapping("/getcoveragedata")
    public Data getcoveragedata(@RequestParam Long taskid){
        String taskID = taskid.toString();
        CoverageData coverageData = new CoverageData();
        File coveragereport = new File(taskID,"coveragereport");
        coveragereport = new File(coveragereport,"index.html");
        if(!coveragereport.exists()){
            return new Data().setCode(200).setData(coverageData);
        }
        File diffcoveragereport = new File(taskID,"diffcoveragereport");
        diffcoveragereport = new File(diffcoveragereport,"index.html");
        if(!diffcoveragereport.exists()){
            return new Data().setCode(200).setData(coverageData);
        }

        Jsouphtml jsouphtml = new Jsouphtml(coveragereport,diffcoveragereport);
        coverageData = jsouphtml.getCoverageData(taskid);
        return new Data().setCode(200).setData(coverageData);
    }


}
