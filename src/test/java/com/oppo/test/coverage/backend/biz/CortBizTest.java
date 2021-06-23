package com.oppo.test.coverage.backend.biz;

import com.oppo.test.coverage.backend.biz.jacoco.ExecutionDataClient;
import com.oppo.test.coverage.backend.model.request.CompilesFileRequest;
import com.oppo.test.coverage.backend.model.request.EcUploadRequest;
import org.apache.http.entity.ContentType;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

import java.io.File;

import static org.junit.Assert.*;

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
    public void getSceneId() {
        Integer sceneId = cortBiz.getSceneId(5);
        System.out.println(sceneId);
        Assert.assertNotNull(sceneId);
    }

    @Test
    public void getPreSignUrl() {
        String preSignUrl = cortBiz.getPreSignUrl("ec","jacocoAll.ec", ContentType.APPLICATION_OCTET_STREAM);
        System.out.println(preSignUrl);
        Assert.assertNotNull(preSignUrl);
    }

    @Test
    public void postCompilesFile() {
        CompilesFileRequest request = new CompilesFileRequest();
        request.setAppCode("ci-demo");
        request.setPackageName("ci-demo");
        request.setCommitId("31a88cb4507a66c63203279d3074f07a22ae7002");
        request.setFileUrl("http://ocs-cn-south.oppoer.me/columbus-file-repo/columbus-repo-202104/ci-demo-20210408-14350741.tar.gz");
    }

    @Test
    public void postEcFile(){
        EcUploadRequest request = new EcUploadRequest();
        request.setAppCode("ci-demo");
        request.setCommitId("31a88cb4507a66c63203279d3074f07a22ae7002");
        request.setPackageName("ci-demo");
        request.setBranchName("master");
        request.setCaseId("847");
        request.setDeviceId("10.177.205.211,10.177.131.132,10.177.131.208");
        request.setSceneId(13595);
        request.setFileKey("jacocoAll-847.ec");
        cortBiz.postEcFile(request);
    }

    @Test
    public  void testGetExecutionData() throws Exception {
        boolean executionData = executionDataClient.getExecutionData("10.176.133.217", 8098, new File("F:\\业务场景\\play35\\cdojacoco.exec"), 3);
//        boolean executionData = executionDataClient.getExecutionData("10.177.245.87", 8098, new File("cdojacoco.exec"), 1);

        System.out.println(executionData);
    }
}