package com.oppo.test.coverage.backend.util.file;

import java.io.File;
import java.util.ArrayList;

public class FileExtractUtil {

    public static void extractFiles(String strPath) {
        File dir = new File(strPath);
        // 该文件目录下文件全部放入数组
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                String fileName = file.getName();

                if (!file.isDirectory() && ((fileName.endsWith(".jar") || fileName.endsWith(".zip")))) {
                    // 判断文件名是否以.jar结尾
                    String sourceFilePath = file.getAbsolutePath();
                    String dirName = fileName.substring(0, fileName.length() - 4);
                    String resultPath = sourceFilePath.replace(fileName, dirName);
                    FileOperateUtil.unZipFiles(sourceFilePath, resultPath, true);
                }
            }
        }
    }

    public static String extractFile(File fileName) throws Exception {
        String sourceFilePath = fileName.getAbsolutePath();
        String name = fileName.getName();
        String dirName = name.substring(0, name.length() - 11);
        String resultPath = sourceFilePath.replace(name, dirName);
        if (name.endsWith(".tar.gz")) {
            FileOperateUtil.unTarGz(fileName, resultPath);
        } else {
            FileOperateUtil.unZipFiles(sourceFilePath, resultPath, true);
        }

        return resultPath;
    }

    public static void main(String[] args) throws Exception {
        FileOperateUtil fileOperateUtil = new FileOperateUtil();
        String basicPath = "D:\\zipfile\\source";
        String rootPath = "D:\\zipfile\\source\\fin-loan-api-1.0-SNAPSHOT-20200721_0251-bin-20200721-7675751.zip";
        String targetPath = "D:\\zipfile\\target";
        FileOperateUtil.delAllFile(targetPath);

        long startTime = System.currentTimeMillis();
        long endTime;
        System.out.println("extract path:" + rootPath);

        File zipfile = new File(rootPath);
        String resultPath = extractFile(zipfile);
        fileOperateUtil.copyFolder(resultPath + "\\lib", targetPath);
        File targetPathFolder = new File(targetPath);
        File[] files = targetPathFolder.listFiles();
        String applicationID = "fin-loan-api";
        ArrayList applicationsrclist = new ArrayList();
        applicationsrclist.add("fin-loan-core");
        applicationsrclist.add("fin-loan-domain");
        File classFile = null;

        if (files != null) {
            int filesNum = files.length;
            for (File file : files) {
                String fileName = file.getName();
                if (!fileName.endsWith("sources.jar")) {
                    String classPath = extractFile(file);
                    File baseclassFile = new File(classPath, "BOOT-INF");
                    classFile = new File(baseclassFile, "classes");
                    String buildversionprex = fileName.substring(applicationID.length());
                    for (Object o : applicationsrclist) {
                        String dependentjar = o.toString() + buildversionprex;
                        fileOperateUtil.copyFile(baseclassFile + File.separator + "lib" + File.separator + dependentjar, classFile + File.separator + dependentjar);
                    }
                    FileOperateUtil.delAllFile(baseclassFile + File.separator + "lib");
                }
            }

            FileOperateUtil.delAllFile(basicPath);
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
        //获取结束时间
        endTime = System.currentTimeMillis();
        System.out.println("程序运行时间: " + (endTime - startTime) / 1000 + "s");
    }
}
