package com.oppo.jacocoreport;

import com.oppo.jacocoreport.coverage.utils.Config;
import com.oppo.jacocoreport.coverage.utils.HttpUtils;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class ApplicationInit implements ApplicationRunner {
    @Override
    public void run(ApplicationArguments args) {
         System.out.println("start recover task");
         HttpUtils.sendGetRequest(Config.RECOVER_TIMERTASK_URL);
    }
}
