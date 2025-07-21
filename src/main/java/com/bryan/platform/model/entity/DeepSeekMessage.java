package com.bryan.platform.model.entity;

import lombok.Data;

/**
 * DeepSeek 消息实体
 *
 * @author Bryan Long
 * @since 2025/6/25 - 11:13
 * @version 1.0
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