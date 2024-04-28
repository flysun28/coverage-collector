package com.oppo.test.coverage.backend;

import com.oppo.test.coverage.backend.util.SystemConfig;
import com.oppo.test.coverage.backend.util.http.HttpUtils;
import esa.restlight.spring.util.SpringContextUtils;
import org.mybatis.spring.annotation.MapperScan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

import java.util.Optional;

/**
 * @author :
 */
@SpringBootApplication
@MapperScan(basePackages = "com.oppo.test.coverage.backend.record.mapper")
public class CoverageBackendApplication {

	private static final Logger LOGGER = LoggerFactory.getLogger(CoverageBackendApplication.class);

	public static void main(String[] args) {
		ApplicationContext context = SpringApplication.run(CoverageBackendApplication.class, args);
		LOGGER.warn("CoverageBackendApplication start ok");

		Optional<SystemConfig> configOptional = SpringContextUtils.getBean(context, SystemConfig.class);
		configOptional.ifPresent(config -> HttpUtils.sendGet(config.getRecoverTimerTaskUrl()));
	}

}
