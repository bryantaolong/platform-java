package com.bryan.platform.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * ClassName: DeepSeekApiProperties
 * Package: com.bryan.platform.config.properties
 * Description:
 * Author: Bryan Long
 * Create: 2025/6/25 - 11:24
 * Version: v1.0
 */
@Component
@ConfigurationProperties(prefix = "deepseek.api")
@Getter
@Setter
public class DeepSeekApiProperties {
    private String key;

    private String url;

    private String model;

    // getters and setters
}
