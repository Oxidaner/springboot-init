package com.zzb.springbootinit.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 全局跨域配置
     * CorsConfig用来解决全局跨域配置问题，可以指定请求方法、是否允许发送Cookie.放行哪些特定域名
     * 或ip、允许哪些请求头。
 * 也可以通过注解@CrossOrigin来实现局部跨域配置
 * @author <a href="https://github.com/Oxidaner">Oxidaner</a>
 * 
 */



@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // 覆盖所有请求
        registry.addMapping("/**")
                // 允许发送 Cookie
                .allowCredentials(true)
                // 放行哪些域名（必须用 patterns，否则 * 会和 allowCredentials 冲突）
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .exposedHeaders("*");
    }
}
