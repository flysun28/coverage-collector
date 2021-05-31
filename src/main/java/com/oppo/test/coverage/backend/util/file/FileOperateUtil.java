package com.oppo.test.coverage.backend.util.file;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Zip;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.tar.TarEntry;
import org.apache.tools.tar.TarInputStream;
import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Enumeration;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipOutputStream;

public class FileOperateUtil {

    private static final Logger logger = LoggerFactory.getLogger(FileOperateUtil.class);

    /**
     * 压缩文件
     *
     * @param zipFilePath 压缩的文件完整名称(目录+文件名)
     * @param srcPathName 需要被压缩的文件或文件夹
     */
    public static File compressFiles(String zipFilePath, String srcPathName) {
        File zipFile = new File(zipFilePath);
        File srcDir = new File(srcPathName);
        if (!srcDir.exists()) {
            logger.warn("需压缩目录不存在 : {}", srcPathName);
            return zipFile;
        }
        Project prj = new Project();
        FileSet fileSet = new FileSet();
        fileSet.setProject(prj);
        if (srcDir.isDirectory()) {
            //是目录
            fileSet.setDir(srcDir);
            //包括哪些文件或文件夹 eg:zip.setIncludes("*.java");
            fileSet.setIncludes("*.*");
            //排除哪些文件或文件夹
            fileSet.setExcludes("*.zip");
        } else {
            fileSet.setFile(srcDir);
        }
        Zip zip = new Zip();
        zip.setProject(prj);
        zip.setDestFile(zipFile);
        //以gbk编码进行压缩，注意windows是默认以gbk编码进行压缩的
        zip.setEncoding("gbk");
        zip.addFileset(fileSet);
        zip.execute();
        return zipFile;
    }

    /**
     * 解压文件到指定目录
     *
     * @param //zipFile 目标文件
     * @param //descDir 解压目录
     * @author isDelete 是否删除目标文件
     */
    @SuppressWarnings("unchecked")
    static void unZipFiles(String zipFilePath, String fileSavePath, boolean isDelete) {
        boolean isUnZipSuccess = true;
        try {
            (new File(fileSavePath)).mkdirs();
            File f = new File(zipFilePath);
            if ((!f.exists()) && (f.length() <= 0)) {
                throw new RuntimeException("not find " + zipFilePath + "!");
            }
            //一定要加上编码，之前解压另外一个文件，没有加上编码导致不能解压
            ZipFile zipFile = new ZipFile(f, "gbk");
            String gbkPath, strtemp;
            Enumeration<ZipEntry> e = zipFile.getEntries();
            while (e.hasMoreElements()) {
                org.apache.tools.zip.ZipEntry zipEnt = e.nextElement();
                gbkPath = zipEnt.getName();
                strtemp = fileSavePath + File.separator + gbkPath;
                if (zipEnt.isDirectory()) {
                    //目录
                    File dir = new File(strtemp);
                    if (!dir.exists()) {
                        dir.mkdirs();
                    }
                } else {
                    // 读写文件
                    InputStream is = zipFile.getInputStream(zipEnt);
                    BufferedInputStream bis = new BufferedInputStream(is);
                    // 建目录
                    for (int i = 0; i < gbkPath.length(); i++) {
                        if ("/".equalsIgnoreCase(gbkPath.substring(i, i + 1))) {
                            String temp = fileSavePath + File.separator + gbkPath.substring(0, i);
                            File subdir = new File(temp);
                            if (!subdir.exists()) {
                                subdir.mkdir();
                            }
                        }
                    }
                    FileOutputStream fos = new FileOutputStream(strtemp);
                    BufferedOutputStream bos = new BufferedOutputStream(fos);
                    int len;
                    byte[] buff = new byte[5120];
                    while ((len = bis.read(buff)) != -1) {
                        bos.write(buff, 0, len);
                    }
                    bos.close();
                    fos.close();
                }
            }
            zipFile.close();
        } catch (Exception e) {
            logger.error("解压文件出现异常: {}, {} - {}", zipFilePath, e.getMessage(), e.getCause());
            isUnZipSuccess = false;
//            fileOperateUtil.WriteStringToFile(fileOperateUtil.logPath, "extract file error: " + zipFilePath);
        }
        //文件不能删除的原因：
        //1.看看是否被别的进程引用，手工删除试试(删除不了就是被别的进程占用)
        //2.file是文件夹 并且不为空，有别的文件夹或文件，
        //3.极有可能有可能自己前面没有关闭此文件的流(我遇到的情况)
        if (isDelete && isUnZipSuccess) {
            boolean flag = new File(zipFilePath).delete();
        }
    }

    /**
     * 删除指定文件夹下所有文件
     *
     * @param path 文件夹完整绝对路径
     */
    public static void delAllFile(String path) {
        File file = new File(path);
        if (!file.exists()) {
            return;
        }
        if (!file.isDirectory()) {
            return;
        }
        String[] tempList = file.list();
        if (tempList == null) {
            return;
        }
        File temp;
        for (String s : tempList) {
            if (path.endsWith(File.separator)) {
                temp = new File(path + s);
            } else {
                temp = new File(path + File.separator + s);
            }
            if (temp.isFile()) {
                temp.delete();
            }
            if (temp.isDirectory()) {
                // 先删除文件夹里面的文件
                delAllFile(path + "/" + s);
                (new File(path + "/" + s)).delete();
            }
        }
    }

    /**
     * 复制单个文件
     *
     * @param oldPath String 原文件路径 如：c:/fqf.txt
     * @param newPath String 复制后路径 如：f:/fqf.txt
     * @return boolean
     */
    public static void copyFile(String oldPath, String newPath) {
        BufferedInputStream bis = null;
        BufferedOutputStream bos = null;
        try {
            bis = new BufferedInputStream(new FileInputStream(oldPath));
            bos = new BufferedOutputStream(new FileOutputStream(newPath));
            int hasRead = 0;
            byte[] b = new byte[2048];
            while ((hasRead = bis.read(b)) > 0) {
                bos.write(b, 0, hasRead);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (bos != null) {
                try {
                    bos.flush();
                    bos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (bis != null) {
                try {
                    bis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 复制整个文件夹内容
     *
     * @param oldPath String 原文件路径 如c:/fqf
     * @param newPath String 复制后路径 如f:/fqf/ff
     */
    public void copyFolder(String oldPath, String newPath) {
        try {
            //如果文件夹不存在 则建立新文件夹
            (new File(newPath)).mkdirs();
            File a = new File(oldPath);
            String[] file = a.list();
            if (file == null) {
                return;
            }
            File temp;
            for (String s : file) {
                if (oldPath.endsWith(File.separator)) {
                    temp = new File(oldPath + s);
                } else {
                    temp = new File(oldPath + File.separator + s);
                }

                if (temp.isFile()) {
                    FileInputStream input = new FileInputStream(temp);
                    FileOutputStream output = new FileOutputStream(newPath + "/" + (temp.getName()).toString());
                    byte[] b = new byte[5120];
                    int len;
                    while ((len = input.read(b)) != -1) {
                        output.write(b, 0, len);
                    }
                    output.flush();
                    output.close();
                    input.close();
                }
                if (temp.isDirectory()) {
                    //如果是子文件夹
                    copyFolder(oldPath + "/" + s, newPath + "/" + s);
                }
            }
        } catch (Exception e) {
            logger.error("复制整个文件夹内容操作出错 : {}", oldPath);
            e.printStackTrace();
        }

    }

    /**
     * 写内容到指定文件
     *
     * @param filePath
     * @param content
     */
    public void WriteStringToFile(String filePath, String content) {
        try {
            FileWriter fw = new FileWriter(filePath, true);
            BufferedWriter bw = new BufferedWriter(fw);
            // 往已有的文件上添加字符串
            bw.write(content + "\r\n");
            bw.close();
            fw.close();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    //------------------------------------------------------------------------------------------------------

    /**
     * 解压tar.gz 文件
     *
     * @param file      要解压的tar.gz文件对象
     * @param outputDir 要解压到某个指定的目录下
     * @throws IOException
     */
    static void unTarGz(File file, String outputDir) throws IOException {
        try (TarInputStream tarIn = new TarInputStream(new GZIPInputStream(
                new BufferedInputStream(new FileInputStream(file))),
                1024 * 2)) {
            //创建输出目录
            createDirectory(outputDir, null);

            TarEntry entry;
            while ((entry = tarIn.getNextEntry()) != null) {

                if (entry.isDirectory()) {
                    //是目录
                    entry.getName();
                    createDirectory(outputDir, entry.getName());
                    //创建空目录
                } else {
                    //是文件
                    File tmpFile = new File(outputDir + "/" + entry.getName());
                    //创建输出目录
                    createDirectory(tmpFile.getParent() + "/", null);
                    try (OutputStream out = new FileOutputStream(tmpFile)) {
                        int length;
                        byte[] b = new byte[2048];
                        while ((length = tarIn.read(b)) != -1) {
                            out.write(b, 0, length);
                        }
                    }
                }
            }
        } catch (IOException ex) {
            throw new IOException("解压归档文件出现异常", ex);
        }
    }

    /**
     * 构建目录
     *
     * @param outputDir
     * @param subDir
     */
    private static void createDirectory(String outputDir, String subDir) {
        File file = new File(outputDir);
        if (!(subDir == null || "".equals(subDir.trim()))) {
            //子目录不为空
            file = new File(outputDir + "/" + subDir);
        }
        if (!file.exists()) {
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            file.mkdirs();
        }
    }

    /**
     * 压缩文件
     *
     * @param sourceFilePath 源文件路径
     * @param zipFilePath    压缩后文件存储路径
     * @param zipFilename    压缩文件名
     */
    public static void compressToZip(String sourceFilePath, String zipFilePath, String zipFilename) {
        File sourceFile = new File(sourceFilePath);
        File zipPath = new File(zipFilePath);
        if (!zipPath.exists()) {
            zipPath.mkdirs();
        }
        File zipFile = new File(zipPath + File.separator + zipFilename);
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile))) {
            writeZip(sourceFile, "", zos,zipFilename);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage(), e.getCause());
        }
    }

    /**
     * 遍历所有文件，压缩
     *
     * @param file       源文件目录
     * @param parentPath 压缩文件目录
     * @param zos        文件流
     * @param zipFilename 压缩文件名,避免重复压缩死循环
     */
    public static void writeZip(File file, String parentPath, ZipOutputStream zos, String zipFilename) {
        if (file.isDirectory()) {
            //目录
            parentPath += file.getName() + File.separator;
            File[] files = file.listFiles();
            for (File f : files) {
                writeZip(f, parentPath, zos, zipFilename);
            }
            return;
        }
        if (file.getName().equals(zipFilename) || !file.getName().endsWith(".class")){
            return;
        }

        //文件
        try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file))) {
            //指定zip文件夹
            ZipEntry zipEntry = new ZipEntry(parentPath + file.getName());
            zos.putNextEntry(zipEntry);
            int len;
            byte[] buffer = new byte[1024 * 10];
            while ((len = bis.read(buffer, 0, buffer.length)) != -1) {
                zos.write(buffer, 0, len);
                zos.flush();
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage(), e.getCause());
        }
    }


    public static void main(String[] args) throws Exception {
//        unTarGz(new File("D:\\coverreport\\browser-feeds-media-service-1.1.0-20200806-20200806-8075471.tar.gz"),"D:\\coverreport");
        //copyFile("F:\\业务场景\\play31\\log_1618383328444.txt","F:\\业务场景\\test.txt");
        //compressFiles("F:\\业务场景\\play\\script\\test.zip","F:\\业务场景\\play\\script");
        //unZipFiles("F:\\业务场景\\play\\script\\test.zip", "F:\\业务场景\\play\\script", true);
        compressToZip("F:\\业务场景\\play\\script","F:\\业务场景\\play\\script","test.zip");
    }
}
