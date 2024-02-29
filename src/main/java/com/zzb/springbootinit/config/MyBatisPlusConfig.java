package com.zzb.springbootinit.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis Plus 配置
 *
 * @author https://github.com/Oxidaner
 */
@Configuration
@MapperScan("com.zzb.springbootinit.mapper") // 指定扫描的路径
public class MyBatisPlusConfig {

    /**
     * 拦截器配置
     *
     * @return
     */
    @Bean // Bean进行组件的注入，然后添加了分页插件
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor(); // 创建拦截器
        // 分页插件
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL)); // 添加分页插件
        /*
        * 设置请求的页面大于最大页后操作， true调回到首页，false 继续请求  默认false
        * paginationInnerInterceptor.setOverflow(false);
        * 设置最大单页限制数量，默认 500 条，-1 不受限制
        * paginationInnerInterceptor.setMaxLimit(500L)
        * */

        return interceptor;
    }
}