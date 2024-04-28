package com.oppo.test.coverage.backend.biz;

import com.oppo.test.coverage.backend.CoverageBackendApplication;
import com.oppo.test.coverage.backend.model.entity.ApplicationCodeInfo;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * description:
 *
 * @author: W9008323 yezp
 * @Date: 2024/4/26 10:58
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = CoverageBackendApplication.class)
public class TaskBizTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(TaskBizTest.class);

    @Autowired
    TaskBiz taskBiz;

    @Test
    @DisplayName("开始覆盖率任务测试")
    public void startCoverageTaskTest() throws InterruptedException {
        for (int i = 0; i < 10; i++) {
            ApplicationCodeInfo applicationCodeInfo = getApplicationCodeInfo(i);
            LOGGER.info("startCoverageTaskTest applicationCodeInfo:{}", applicationCodeInfo);
            taskBiz.addTaskToQueue(applicationCodeInfo);
        }

        Thread.sleep(5000L);
    }

    private ApplicationCodeInfo getApplicationCodeInfo(int index) {
        ApplicationCodeInfo applicationCodeInfo = new ApplicationCodeInfo();
        applicationCodeInfo.setGitPath("gitlab.com");
        applicationCodeInfo.setTestedBranch("feature_" + index);
        applicationCodeInfo.setTestedCommitId("c837123561dfgweaq");
        applicationCodeInfo.setBasicBranch("master");
        applicationCodeInfo.setBasicCommitId("9dfe3dg354fg3sdfds");
        applicationCodeInfo.setVersionName("v5.0");
        return applicationCodeInfo;
    }

}
