package com.oppo.jacocoreport.controller;

import com.oppo.jacocoreport.coverage.jacoco.AnalyNewBuildVersion;
import com.oppo.jacocoreport.coverage.utils.Config;
import com.oppo.jacocoreport.coverage.utils.FileOperateUtil;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;

@Component
public class DeleteCoverageTaskScheduled {
    @Scheduled(cron="* 5 1 * * ?")
    public void scheduledTask(){
        File taskPath = new File(Config.ReportBasePath,"taskID");
        System.out.println("start delete coverage task");
        if(taskPath.isDirectory()){
           File[] tasklist = taskPath.listFiles();
           for(File taskidPath:tasklist) {
               if (!AnalyNewBuildVersion.fileNotUpdateByHours(taskidPath,240)) {
                   File reportAllCovDirectory = new File(taskidPath, "coveragereport");////要保存报告的地址
                   File reportDiffDirectory = new File(taskidPath, "coveragediffreport");
                   File filterreportAllCovDirectory = new File(taskidPath, "filtercoveragereport");////要保存报告的地址
                   File filterreportDiffDirectory = new File(taskidPath, "filtercoveragediffreport");
                   FileOperateUtil.delAllFile(reportAllCovDirectory.toString());
                   FileOperateUtil.delAllFile(reportDiffDirectory.toString());
                   FileOperateUtil.delAllFile(filterreportAllCovDirectory.toString());
                   FileOperateUtil.delAllFile(filterreportDiffDirectory.toString());
               }
           }
        }
    }
    public static void main(String args[]){
        DeleteCoverageTaskScheduled deleteCoverageTaskScheduled = new DeleteCoverageTaskScheduled();
        deleteCoverageTaskScheduled.scheduledTask();
    }
}
