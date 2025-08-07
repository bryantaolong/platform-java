package com.bryan.platform.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * JpaConfig
 *
 * @author Bryan Long
 * @version 1.0
 * @since 2025/8/7
 */
@Configuration
@EntityScan("com.bryan.platform.model.entity")
@EnableJpaAuditing
@EnableTransactionManagement
public class JpaConfig {
}
