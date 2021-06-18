package com.oppo.test.coverage.backend.controller;

import com.oppo.trace.servlet.TraceFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class ESATraceFilter {
    @Bean
    public FilterRegistrationBean traceFilter() {
        FilterRegistrationBean registration = new FilterRegistrationBean();
        registration.setFilter(new TraceFilter());
        registration.addUrlPatterns("/*");
        registration.setName("traceFilter");
        registration.setOrder(1);
        return registration;
    }
}
