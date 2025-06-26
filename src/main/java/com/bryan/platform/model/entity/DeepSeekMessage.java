package com.bryan.platform.model.entity;

import lombok.Data;

/**
 * ClassName: DeepSeekMessage
 * Package: com.bryan.platform.model.entity
 * Description:
 * Author: Bryan Long
 * Create: 2025/6/25 - 11:13
 * Version: v1.0
 */
@Data
public class DeepSeekMessage {
    private String role;
    private String content;

    public DeepSeekMessage(String role, String content) {
        this.role = role;
        this.content = content;
    }
}