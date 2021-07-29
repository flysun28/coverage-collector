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
                    FileOperateUtil.unZipJarFiles(sourceFilePath, resultPath, true);
                }
            }
        }
    }


    public static String extractFile(File fileName) throws Exception {
        // /home/service/app/coveragebackend/xxxxx/taskID/1049/downloadzip/ci-demo-20210408-1435xxxxxxxxxx
        String sourceFilePath = fileName.getAbsolutePath();

        // ci-demo-20210408-1435xxxxxxxxxx
        String name = fileName.getName();

        // ci-demo-20210408-1435
        String dirName = name.substring(0, name.length() - 11);

        // /home/service/app/coveragebackend/xxxxx/taskID/1049/downloadzip/ci-demo-20210408-1435
        String resultPath = sourceFilePath.replace(name, dirName);

        if (name.endsWith(".tar.gz")) {
            FileOperateUtil.unTarGz(fileName, resultPath);
        } else {
            FileOperateUtil.unZipFiles(sourceFilePath, resultPath, true);
        }

        return resultPath;
    }

    public static void main(String[] args) throws Exception {
        extractFiles("F:\\业务场景\\play39\\dataland-cube-web-20210728081834-20210728-73380951.tar\\BOOT-INF");
    }
}
