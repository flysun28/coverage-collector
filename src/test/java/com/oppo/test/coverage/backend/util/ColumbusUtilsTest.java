package com.oppo.test.coverage.backend.util;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

import static org.junit.Assert.*;

/**
 * @author 80264236
 * @date 2021/4/16 17:10
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class ColumbusUtilsTest {

    @Resource
    ColumbusUtils columbusUtils;

    @Test
    public void getAppDeployInfoFromBuildVersionList(){
        System.out.println(columbusUtils.getAppDeployInfoFromBuildVersionList("jits-open-api","jits-open-api-20210514165809-39",1, null, null));
    }

    @Test
    public void downloadColumbusBuildVersion(){
        String repositoryUrl = "columbus-file-repo/columbus-repo-202105/jits-open-api-4.0.1-SNAPSHOT-20210514_0858-bin-20210514-15317941.zip";
        String downloadPath = "F:\\业务场景\\play36";
        System.out.println(columbusUtils.downloadColumbusBuildVersion(repositoryUrl,downloadPath));
    }

    @Test
    public void getBuildVersionList(){
        System.out.println(columbusUtils.getBuildVersionList("cmdb-api","cmdb-api-20210622161611-82",3));
    }

}