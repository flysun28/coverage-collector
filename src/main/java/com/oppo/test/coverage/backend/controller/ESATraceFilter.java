package com.oppo.test.coverage.backend.controller;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import com.oppo.trace.servlet.TraceFilter;

@Component
public class ESATraceFilter {
    @Bean
    public FilterRegistrationBean traceFilter() {
        FilterRegistrationBean registration = new FilterRegistrationBean();
        System.out.println("ESA trace start");
        registration.setFilter(new TraceFilter());
        registration.addUrlPatterns("/*");
        registration.setName("traceFilter");
        registration.setOrder(1);
        return registration;
    }
}
