package com.oppo.test.coverage.backend.util;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.*;

/**
 * @author 80264236
 * @date 2021/4/16 17:10
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class ColumbusUtilsTest {

    @Test
    public void test(){
        System.out.println(ColumbusUtils.getAppDeployInfoFromBuildVersionList("ci-demo","ci-demo-20210326163909-181",1));
    }

}