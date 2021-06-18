package com.oppo.test.coverage.backend;

import com.oppo.test.coverage.backend.util.SystemConfig;
import com.oppo.test.coverage.backend.util.http.HttpUtils;
import esa.restlight.spring.util.SpringContextUtils;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.Optional;

/**
 * @author :
 */
@SpringBootApplication
@EnableAsync
@EnableScheduling
@MapperScan(basePackages = "com.oppo.test.coverage.backend.record.mapper")
public class CoverageBackendApplication {

	public static void main(String[] args) {
		ApplicationContext context = SpringApplication.run(CoverageBackendApplication.class, args);

		Optional<SystemConfig> configOptional = SpringContextUtils.getBean(context,SystemConfig.class);
		configOptional.ifPresent(config -> HttpUtils.sendGet(config.getRecoverTimerTaskUrl()));
	}

}
