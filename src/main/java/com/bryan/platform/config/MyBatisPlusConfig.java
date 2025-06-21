package com.bryan.platform.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * ClassName: MyBatisPlusConfig
 * Package: com.bryan.platform.config
 * Description:
 * Author: Bryan Long
 * Create: 2025/6/19 - 19:53
 * Version: v1.0
 */
@Configuration
@MapperScan("com.bryan.platform.dao.mapper") // 扫描 Mapper 接口
@EnableTransactionManagement // 开启事务管理支持
public class MyBatisPlusConfig {

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();

        // 乐观锁插件
        interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());
        // 如果配置多个插件, 分页插件需要最后添加
        // 如果有多数据源可以不配具体类型, 否则都建议加上具体的 DbType
//        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));

        return interceptor;
    }
}
