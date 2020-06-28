package com.oppo.test.jacocoreport.controller;

import com.oppo.test.jacocoreport.entity.ApplicationCodeInfo;
import com.oppo.test.jacocoreport.entity.Data;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CoverageController {

    @GetMapping("/startcoverage")
    public Data say(ApplicationCodeInfo applicationCodeInfo){
        return dealWith(applicationCodeInfo);
    }

    private Data dealWith(ApplicationCodeInfo applicationCodeInfo){
         String gitPath = applicationCodeInfo.getGitPath();
         String testedBranch = applicationCodeInfo.getTestedBranch();
         String basicBranch = applicationCodeInfo.getBasicBranch();
         String environment = applicationCodeInfo.getEnvironment();
         if(StringUtils.isEmpty(gitPath)){
             return new Data().setCode(-1).setResult("gitpath can not be blank");
         }
        if(StringUtils.isEmpty(testedBranch)){
            return new Data().setCode(-2).setResult("testedBranch can not be blank");
        }
        if(StringUtils.isEmpty(basicBranch)){
            return new Data().setCode(-3).setResult("basicBranch can not be blank");
        }
        if(StringUtils.isEmpty(environment)){
            return new Data().setCode(-4).setResult("environment can not be blank");
        }
         return new Data().setCode(200).setResult(gitPath);
    }
}
