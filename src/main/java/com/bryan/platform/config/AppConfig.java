package com.bryan.platform.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * * AppConfig Application configuration class
 * *
 * * @author Bryan Long
 * * @version 1.0
 * * @since 2025/6/25-11:20
 */

@Configuration
public class AppConfig {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
