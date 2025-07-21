package com.bryan.platform.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * DeepSeekApiProperties
 *
 * @author Bryan Long
 * @version 1.0
 * @since 2025/6/25 - 11:24
 */
@Component
@ConfigurationProperties(prefix = "deepseek.api")
@Getter
@Setter
public class DeepSeekApiProperties {
    private String key;

    private String url;

    private String model;
}
