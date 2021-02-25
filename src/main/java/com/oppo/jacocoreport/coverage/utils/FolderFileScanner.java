package com.oppo.jacocoreport.coverage.utils;

import com.amazonaws.services.s3.AmazonS3;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import org.springframework.util.CollectionUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author 80264236
 * @date 2021/2/9 10:30
 */
public class FolderFileScanner {

    private static final List<String> REPORT_EXTENSION_NAME = Lists.newArrayList("html","css","js","gif");
    private static final List<String> FILE_UPLOAD_EXTENSION_NAME = Lists.newArrayList("html","css","js","gif","exec","class");

    /**
     * 执行前下载对应文件
     *@param projectName : 项目名称,分支覆盖率数据目录
     *@param taskId : 任务id，覆盖率任务相关目录
     * */
    public static void fileDownLoad(String projectName,Long taskId){
        List<String> fileList = getDownloadFileList(projectName, taskId);
        AmazonS3 s3 = OcsUtil.getAmazonS3();
        for (String fileName : fileList){
            OcsUtil.download(s3,fileName,fileName);
        }
    }

    private static List<String> getDownloadFileList(String projectName,Long taskId){
        String taskPath = Config.ReportBasePath + "/taskID/" + taskId;
        String branchPath = Config.ReportBasePath + "/projectCovPath/" + projectName;

        List<String> result = new ArrayList<>(OcsUtil.query(taskPath));
        result.addAll(OcsUtil.query(branchPath));
        return result;
    }


    /**
     * 轮询过程上传报告文件
     *@param projectName : git项目名称,分支覆盖率数据目录
     *@param taskId : 任务id，覆盖率任务相关目录
     * */
    public static void reportUpload(String projectName,Long taskId){
        ArrayList<File> uploadFileList = getUploadFileList(projectName, taskId, false);
        AmazonS3 s3 = OcsUtil.getAmazonS3();
        for (File file : uploadFileList){
            List<String> splitList = Splitter.on(".").trimResults().splitToList(file.getName());
            String extensionName = splitList.get(splitList.size()-1);
            OcsUtil.upload(s3,file.getAbsolutePath(),file,"html".equals(extensionName)?"text/html":null);
        }
    }

    /**
     * 上传分支覆盖率报告文件
     *@param taskId : 任务id，覆盖率任务相关目录
     * */
    public static void branchReportUpload(Long taskId){
        ArrayList<File> uploadFileList = getUploadFileList("null", taskId, false);
        AmazonS3 s3 = OcsUtil.getAmazonS3();
        for (File file : uploadFileList){
            List<String> splitList = Splitter.on(".").trimResults().splitToList(file.getName());
            String extensionName = splitList.get(splitList.size()-1);
            OcsUtil.upload(s3,file.getAbsolutePath(),file,"html".equals(extensionName)?"text/html":null);
        }
    }



    /**
     * 执行完毕上传对应文件
     *@param projectName : git项目名称,分支覆盖率数据目录
     *@param taskId : 任务id，覆盖率任务相关目录
     * */
    public static void fileUpload(String projectName,Long taskId){
        ArrayList<File> uploadFileList = getUploadFileList(projectName, taskId, true);
        AmazonS3 s3 = OcsUtil.getAmazonS3();
        for (File file : uploadFileList){
            List<String> splitList = Splitter.on(".").trimResults().splitToList(file.getName());
            String extensionName = splitList.get(splitList.size()-1);
            OcsUtil.upload(s3,file.getAbsolutePath(),file,"html".equals(extensionName)?"text/html":null);
        }
        deleteAllFileAfterUpload(projectName, taskId);
    }

    private static void deleteAllFileAfterUpload(String projectName, Long taskId){
        String taskPath = Config.ReportBasePath + "/taskID/" + taskId;
        String branchPath = Config.ReportBasePath + "/projectCovPath/" + projectName;
        FileOperateUtil.delAllFile(taskPath);
        FileOperateUtil.delAllFile(branchPath);
    }

    private static ArrayList<File> getUploadFileList(String projectName, Long taskId, boolean timerFinished){
        ArrayList<File> result = new ArrayList<>();
        String taskPath = Config.ReportBasePath + "/taskID/" + taskId;
        String branchPath = Config.ReportBasePath + "/projectCovPath/" + projectName;
        ArrayList<File> tempFileList = scanFilesWithRecursion(taskPath,timerFinished);
        if (!CollectionUtils.isEmpty(tempFileList)){
            result.addAll(tempFileList);
        }
        tempFileList = scanFilesWithRecursion(branchPath,timerFinished);
        if (!CollectionUtils.isEmpty(tempFileList)){
            result.addAll(tempFileList);
        }
        return result;
    }


    /**
     * 递归扫描指定文件夹下面的指定文件
     * @param folderPath : 文件目录
     * @param timerFinished : 是否任务结束,若结束则传所有文件,否则只上传报告
     * @return : 文件列表
     */
    private static ArrayList<File> scanFilesWithRecursion(String folderPath,boolean timerFinished){

        ArrayList<File> scanFile = new ArrayList<File>();

        File directory = new File(folderPath);
        //不是目录直接返回
        if (!directory.isDirectory()) {
            return scanFile;
        }
        //取到目录下所有文件
        File[] fileList = directory.listFiles();
        if (fileList==null || fileList.length<1){
            return scanFile;
        }
        // 遍历文件,是文件夹则递归,是文件则获取
        for (File file : fileList) {
            if (file.isDirectory()) {
                scanFile.addAll(Objects.requireNonNull(scanFilesWithRecursion(file.getAbsolutePath(),timerFinished)));
            }
            else {
                //筛选指定的需要上传的文件
                List<String> splitList = Splitter.on(".").trimResults().splitToList(file.getName());
                String extensionName = splitList.get(splitList.size()-1);

                if (isUploadFile(extensionName,timerFinished)){
                    scanFile.add(file);
                }
            }
        }
        return scanFile;
    }

    private static boolean isUploadFile(String extensionName,boolean timerFinished){
        //未完成,只上传报告
        if (!timerFinished && REPORT_EXTENSION_NAME.contains(extensionName)){
            return true;
        }
        //完成后,全部上传
        if (timerFinished && FILE_UPLOAD_EXTENSION_NAME.contains(extensionName)){
            return true;
        }
        return false;
    }

    public static void main(String[] args) {
        ArrayList<File> result = FolderFileScanner.scanFilesWithRecursion("D:\\文档\\培训课件\\培训材料准备\\2",true);
        AmazonS3 s3 = OcsUtil.getAmazonS3();
        for (File file : result){
            List<String> splitList = Splitter.on(".").trimResults().splitToList(file.getName());
            String extensionName = splitList.get(splitList.size()-1);
            String res = file.getAbsolutePath()
                    .replace("D:\\文档\\培训课件\\培训材料准备\\2","/home/service/app/coveragebackend/fawoknqovs7v/taskID/15483")
                    .replace("\\","/");

            System.out.println(res);

            OcsUtil.upload(s3,res,file,"html".equals(extensionName)?"text/html":null);

        }
    }

}
