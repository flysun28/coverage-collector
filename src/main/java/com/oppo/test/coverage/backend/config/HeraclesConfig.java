package com.oppo.test.coverage.backend.config;

import com.oppo.basic.heracles.client.core.spring.annotation.HeraclesDynamicConfig;
import org.springframework.stereotype.Component;

/**
 * description:配置中心
 *
 * @author: W9008323 yezp
 * @Date: 2024/4/26 10:07
 */
@Component
public class HeraclesConfig {

    public static final String APPLICATION_PROPERTIES = "application.properties";
    private final int availableProcessors = Runtime.getRuntime().availableProcessors();
    private final int defaultCorePoolSize = availableProcessors >= 4 ? availableProcessors : availableProcessors * 2;
    private final int defaultMaxPoolSize = availableProcessors >= 4 ? availableProcessors : availableProcessors * 2;

    @HeraclesDynamicConfig(key = "coverage.task.core.pool.size", fileName = APPLICATION_PROPERTIES, defaultValue = "4")
    Integer coverageTaskCorePoolSize;

    public Integer getCoverageTaskCorePoolSize() {
        return coverageTaskCorePoolSize == null ? defaultCorePoolSize : coverageTaskCorePoolSize;
    }

    @HeraclesDynamicConfig(key = "coverage.task.max.pool.size", fileName = APPLICATION_PROPERTIES, defaultValue = "4")
    Integer coverageTaskMaxPoolSize;

    public Integer getCoverageTaskMaxPoolSize() {
        return coverageTaskMaxPoolSize == null ? defaultMaxPoolSize : coverageTaskMaxPoolSize;
    }

    @HeraclesDynamicConfig(key = "coverage.task.queue.size", fileName = APPLICATION_PROPERTIES, defaultValue = "128")
    Integer coverageTaskQueueSize;

    public Integer getCoverageTaskQueueSize() {
        return coverageTaskQueueSize;
    }
}
