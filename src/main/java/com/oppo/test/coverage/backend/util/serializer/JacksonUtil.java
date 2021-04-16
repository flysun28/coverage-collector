package com.oppo.test.coverage.backend.util.serializer;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import esa.restlight.core.serialize.HttpBodySerializer;
import esa.restlight.core.serialize.HttpJsonBodySerializerAdapter;
import esa.restlight.core.serialize.JacksonSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.text.SimpleDateFormat;

/**
 * @author 80264236
 * @date 2021/4/16 15:07
 */
@Configuration
public class JacksonUtil {

    @Bean
    public HttpBodySerializer bodySerializer(){
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,false);
        return new HttpJsonBodySerializerAdapter(new JacksonSerializer(objectMapper)) {};
    }

}
