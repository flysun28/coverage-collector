package com.oppo.jacocoreport.controller;

import com.oppo.jacocoreport.coverage.jacoco.AnalyNewBuildVersion;
import com.oppo.jacocoreport.coverage.utils.Config;
import com.oppo.jacocoreport.coverage.utils.FileOperateUtil;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;

@Component
public class DeleteCoverageTaskScheduled {
//    @Scheduled(cron="* 5 1 * * *")
    @Scheduled(fixedRate = 120000)
    public void scheduledTask(){
        File taskPath = new File(Config.ReportBasePath,"taskID");
        if(taskPath.isDirectory()){
           File[] tasklist = taskPath.listFiles();
           for(File taskid:tasklist) {
               if (!AnalyNewBuildVersion.fileNotUpdateByHours(taskid,48)) {
                   FileOperateUtil.delAllFile(taskid.toString());
               }
           }
        }
    }
    public static void main(String args[]){
        DeleteCoverageTaskScheduled deleteCoverageTaskScheduled = new DeleteCoverageTaskScheduled();
        deleteCoverageTaskScheduled.scheduledTask();
    }
}
