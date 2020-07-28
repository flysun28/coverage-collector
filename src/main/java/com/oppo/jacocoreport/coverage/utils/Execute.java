package com.oppo.jacocoreport.coverage.utils;

import java.io.File;
import java.util.ArrayList;

public class Execute {
    public void extractFiles(String strPath) {
        FileOperateUtil fileOperateUtil = new FileOperateUtil();
        File dir = new File(strPath);
        File[] files = dir.listFiles(); // 该文件目录下文件全部放入数组
        if (files != null) {
            for (int i = 0; i < files.length; i++) {
                String fileName = files[i].getName();

                if (!files[i].isDirectory() && (fileName.endsWith(".jar") || fileName.endsWith(".zip"))) { // 判断文件名是否以.jar结尾
                    String sourceFilePath = files[i].getAbsolutePath();
                    fileOperateUtil.WriteStringToFile(fileOperateUtil.getLogPath(),"extract file:"+sourceFilePath);
                    String dirName = fileName.substring(0,fileName.length() -4);
                    String resultPath = sourceFilePath.replace(fileName,dirName);
                    fileOperateUtil.unZipFiles(sourceFilePath,resultPath,true);
                }
            }
        }
    }
    public String extractFile(File fileName) throws Exception{
        FileOperateUtil fileOperateUtil = new FileOperateUtil();
        String sourceFilePath = fileName.getAbsolutePath();
        String name = fileName.getName();
        String dirName = name.substring(0,name.length() -11);
        String resultPath = sourceFilePath.replace(name,dirName);
        if(name.endsWith(".zip")){
            fileOperateUtil.unZipFiles(sourceFilePath,resultPath,true);
        }
        else if(name.endsWith(".tar.gz")){
            fileOperateUtil.unTarGz(fileName,resultPath);
        }

        return resultPath;
    }

    public static void main(String[] args) throws Exception{
        FileOperateUtil fileOperateUtil = new FileOperateUtil();
        String basicPath = "D:\\zipfile\\source";
        String rootPath = "D:\\zipfile\\source\\fin-loan-api-1.0-SNAPSHOT-20200721_0251-bin-20200721-7675751.zip";
        if("".equals(rootPath) || rootPath == null){
          System.out.println("please input extract path:");
        }
        String targetPath = "D:\\zipfile\\target";
        fileOperateUtil.delAllFile(targetPath);
        if(rootPath.endsWith("\\")){
          rootPath = rootPath.substring(0,rootPath.length()-1);
        }

        long startTime = System.currentTimeMillis();
        long endTime = 0;
        System.out.println("extract path:"+rootPath);

        Execute execute = new Execute();

        File zipfile = new File(rootPath);
        String resultPath = execute.extractFile(zipfile);
        fileOperateUtil.copyFolder(resultPath+"\\lib",targetPath);
        File targetPathFolder = new File(targetPath);
        File[] files = targetPathFolder.listFiles();
        String applicationID = "fin-loan-api";
       ArrayList applicationsrclist = new ArrayList();
       applicationsrclist.add("fin-loan-core");
       applicationsrclist.add("fin-loan-domain");
        File classFile = null;

        if(files != null){
            int filesNum = files.length;
            for(int i =0;i<filesNum;i++) {
                String fileName = files[i].getName();
                if (!fileName.endsWith("sources.jar")) {
                    String classPath = execute.extractFile(files[i]);
                    File baseclassFile = new File(classPath, "BOOT-INF");
                    classFile = new File(baseclassFile, "classes");
                    String buildversionprex = fileName.substring(applicationID.length());
                    for (int j = 0; j < applicationsrclist.size(); j++) {
                        String dependentjar = applicationsrclist.get(j).toString() + buildversionprex;
                        fileOperateUtil.copyFile(baseclassFile + File.separator + "lib" + File.separator + dependentjar, classFile + File.separator + dependentjar);
                    }
                    fileOperateUtil.delAllFile(baseclassFile + File.separator + "lib");
                }
            }

         fileOperateUtil.delAllFile(basicPath);
//                if(!fileName.endsWith(".svn") && !fileName.endsWith("extractLog.txt") && !fileName.endsWith("target")){
//                     if(files[i].isDirectory()){
//                        fileOperateUtil.copyFolder(rootPath+"\\"+fileName,rootPath+"\\target\\"+fileName);
//                        execute.extractFiles(rootPath+"\\target\\"+fileName);
////                         File lib = new File(rootPath+"\\target\\"+fileName+"\\lib");
////                         File[] libfiles = lib.listFiles();
////                         for(int j=0;j <libfiles.length;j++){
////                              String jarFile = libfiles[i].getName();
////                              if(!jarFile.endsWith("sources.jar")){
////                                  execute.extractFile(libfiles[i]);
////                              }
////                         }
//                     }else{
////                         fileOperateUtil.copyFile(rootPath+"\\"+fileName,rootPath+"\\target\\"+fileName);
//                         execute.extractFile(new File(rootPath));
//                     }
//                }
//            }
        }
        System.out.println(classFile.toString());
        endTime = System.currentTimeMillis();//获取结束时间
        System.out.println("程序运行时间: "+(endTime - startTime)/ 1000 +"s");
    }
}
