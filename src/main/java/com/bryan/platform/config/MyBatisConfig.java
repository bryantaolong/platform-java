package com.bryan.platform.config;

import com.bryan.platform.handler.AuditFieldInterceptor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * MyBatisPlusConfig
 *
 * @author Bryan Long
 */
@Configuration
@MapperScan("com.bryan.platform.mapper") // 扫描 Mapper 接口
@EnableTransactionManagement // 开启事务管理支持
public class MyBatisConfig {

    @Bean
    public AuditFieldInterceptor auditFieldInterceptor() {
        return new AuditFieldInterceptor();
    }
}
