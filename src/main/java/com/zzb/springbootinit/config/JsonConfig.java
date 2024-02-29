package com.zzb.springbootinit.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import org.springframework.boot.jackson.JsonComponent;
import org.springframework.context.annotation.Bean;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

/**
 * Spring MVC Json 配置
 *
 * @author <a href="https://github.com/Oxidaner">Oxidaner</a>
 * 
 */
@JsonComponent // @JsonComponent作用：自定义序列化和反序列SON数据，Spring Boot默认使用JackSon进行序列化和反序列化。
public class JsonConfig {

    /**
     * 添加 Long 转 json 精度丢失的配置
     * 精度丢失场景：id在数据库是 Biglnteger类型，雪花算法生成id大于17位，因此在序列化的时候会产生精度丢失。
     *
     * 怎么防止丢失？用@Bean覆盖组件后，重写逻辑代码，将包装类 Long和基础数据类型 long转化成字符
     */
    @Bean
    public ObjectMapper jacksonObjectMapper(Jackson2ObjectMapperBuilder builder) {
        ObjectMapper objectMapper = builder.createXmlMapper(false).build();
        SimpleModule module = new SimpleModule();
        module.addSerializer(Long.class, ToStringSerializer.instance);
        module.addSerializer(Long.TYPE, ToStringSerializer.instance);
        objectMapper.registerModule(module);
        return objectMapper;
    }
}