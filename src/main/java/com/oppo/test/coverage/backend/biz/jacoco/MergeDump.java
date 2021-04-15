package com.oppo.test.coverage.backend.biz.jacoco;

import org.jacoco.core.tools.ExecFileLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MergeDump {

    private static final Logger logger = LoggerFactory.getLogger(MergeDump.class);

    private final String path;
    private final File destFile;

    public MergeDump(String path) {
        this.path = path;
        this.destFile = new File(path, "jacocoAll.exec");
    }

    private static List<File> fileSets(String dir) {
        List<File> fileSetList = new ArrayList<File>();
        File path = new File(dir);
        if (!path.exists()) {
            logger.error("No path name is : {}", dir);
            return null;
        }
        File[] files = path.listFiles();
        try {
            if (files == null || files.length == 0) {
                return null;
            }
        } catch (NullPointerException npe) {
            npe.printStackTrace();
        }

        for (File file : files) {
            if (file.getName().contains(".exec")) {
                fileSetList.add(file);
            }
        }
        return fileSetList;
    }

    public File executeMerge() {
        final ExecFileLoader loader = new ExecFileLoader();
        List<File> fileList = fileSets(this.path);
        if (fileList == null || fileList.size() == 0) {
            return null;
        }
        //如果没有获取新覆盖率文件，就不merge
        if (fileList.size() == 1) {
            if (fileList.get(0).getName().contains("jacocoAll.exec")) {
                return null;
            }
        }
        load(loader, fileList);
        save(loader);
        //执行完成后，删除非必须的dump文件
        for (final File fileSet : fileList) {
            if (!fileSet.getName().contains("jacocoAll.exec")) {
                fileSet.delete();
            }
        }
        return this.destFile;
    }

    /**
     * 加载dump文件
     *
     * @param loader
     */
    private void load(final ExecFileLoader loader, List<File> fileList) {
        for (final File fileSet : fileList) {
            final File inputFile = new File(this.path, fileSet.getName());
            if (inputFile.isDirectory()) {
                continue;
            }
            try {
                loader.load(inputFile);
            } catch (final IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void save(final ExecFileLoader loader) {
        if (loader.getExecutionDataStore().getContents().isEmpty()) {
            logger.warn("Skipping JaCoCo merge execution due to missing execution data files : {}", path);
            return;
        }
        try {
            loader.save(this.destFile, false);
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {
        MergeDump mergeDump = new MergeDump("D:\\codeCoverage\\10010");
        mergeDump.executeMerge();
    }
}
