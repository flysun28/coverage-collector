package com.oppo.test.coverage.backend.util.http;

import esa.httpclient.core.HttpClient;
import esa.httpclient.core.HttpClientBuilder;
import org.springframework.stereotype.Component;

/**
 * @author 80264236
 * @date 2021/4/1 20:10
 */
@Component
public class HttpClientUtil {

    private static HttpClient httpClient = null;

    public HttpClientUtil(){
        HttpClientBuilder builder = new HttpClientBuilder();
        builder.connectTimeout(60000);
        builder.readTimeout(60000);
        httpClient = builder.build();
    }

    public static HttpClient getHttpClient() {
        return httpClient;
    }
}
