package com.bryan.platform.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * ClassName: AppConfig
 * Package: com.bryan.platform.config
 * Description:
 * Author: Bryan Long
 * Create: 2025/6/25 - 11:20
 * Version: v1.0
 */
@Configuration
public class AppConfig {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
