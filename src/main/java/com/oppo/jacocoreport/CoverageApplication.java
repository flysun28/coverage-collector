package com.oppo.jacocoreport;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class CoverageApplication {

    public static void main(String[] args) {
        SpringApplication.run(CoverageApplication.class, args);
    }

}
