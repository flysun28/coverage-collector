package com.oppo.test.coverage.backend.biz;

import com.oppo.test.coverage.backend.biz.jacoco.ExecutionDataClient;
import com.oppo.test.coverage.backend.model.request.EcUploadRequest;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.io.File;

/**
 * @author 80264236
 * @date 2021/5/24 14:56
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class CortBizTest {

    @Resource
    CortBiz cortBiz;

    @Autowired
    ExecutionDataClient executionDataClient;

    @Test
    public void getServerTimestampTest() {
        Long timeStamp = cortBiz.getServerTimestamp();
        System.out.println(timeStamp);
        Assert.assertNotNull(timeStamp);
    }

    @Test
    public void postEcFile() {
        EcUploadRequest request = new EcUploadRequest();
        request.setAppCode("ci-demo");
        request.setCommitId("31a88cb4507a66c63203279d3074f07a22ae7002");
        request.setPackageName("ci-demo");
        request.setBranchName("master");
        request.setCaseId("847");
        request.setSceneId(13595);
        request.setFileKey("jacocoAll-847.ec");
        cortBiz.postEcFile(request, 1);
    }

    @Test
    public void testGetExecutionData() throws Exception {
        boolean executionData = executionDataClient.getEcData("10.177.205.211", 8098, new File("F:\\file-temp\\code-coverage\\atms\\temp\\cdojacoco.exec"), 1);
        System.out.println(executionData);
    }
}