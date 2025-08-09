package com.bryan.platform.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * JpaConfig
 *
 * @author Bryan Long
 */
@Configuration
@EntityScan("com.bryan.platform.domain.entity")
@EnableJpaAuditing
@EnableTransactionManagement
public class JpaConfig {
}
