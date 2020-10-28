package com.oppo.jacocoreport;

import com.oppo.jacocoreport.coverage.utils.Config;
import com.oppo.jacocoreport.coverage.utils.HttpUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableAsync
@EnableScheduling
public class CoverageApplication {

    public static void main(String[] args) {

        SpringApplication.run(CoverageApplication.class, args);
        System.out.println("start recover task");
        HttpUtils.sendGetRequest(Config.RECOVER_TIMERTASK_URL);
    }

}
