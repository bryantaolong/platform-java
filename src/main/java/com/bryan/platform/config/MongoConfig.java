package com.bryan.platform.config;

import com.bryan.platform.domain.entity.post.Post;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.IndexOperations;
import org.springframework.data.mongodb.core.index.TextIndexDefinition;
import org.springframework.data.mongodb.core.index.TextIndexDefinition.TextIndexDefinitionBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.boot.context.event.ApplicationReadyEvent;

/**
 * MongoConfig MongoDB 配置类，用于创建文本索引和启用审计功能。
 *
 * @author Bryan Long
 */
@Slf4j
@Configuration
@EnableMongoAuditing // 启用 Spring Data MongoDB 的审计功能，自动填充 @CreatedDate 和 @LastModifiedDate
@EnableCaching // 启用 Spring Cache，如果你想在这里统一配置缓存，也可以在主启动类上
public class MongoConfig {

    // 自动注入 Spring Boot 自动配置的 MongoTemplate
    // 我们不需要手动创建 MongoTemplate bean，直接使用自动配置的即可
    @Autowired
    private MongoTemplate mongoTemplate;

    /**
     * 在应用启动并完全加载所有 Bean 后，确保 MongoDB 集合的文本索引存在。
     * 使用 @EventListener(ApplicationReadyEvent.class) 确保在应用就绪后执行。
     */
    @EventListener(ApplicationReadyEvent.class)
    public void ensureMongoTextIndexesOnStartup() {
        // 获取 Post 集合的索引操作对象
        IndexOperations indexOps = mongoTemplate.indexOps(Post.class);

        // 定义文本索引
        TextIndexDefinition textIndex = new TextIndexDefinitionBuilder()
                .onField("title", 2F) // title 字段权重为 2.0
                .onField("content") // content 字段默认权重 1.0
                .build();

        // 确保文本索引存在。使用 createIndex 替代 deprecated 的 ensureIndex
        // 如果索引已存在，此操作幂等（不会重复创建）
        indexOps.createIndex(textIndex);

        // 可以在这里添加日志，确认索引是否已创建或存在
         log.info("MongoDB text index for Post collection ensured.");
    }
}
