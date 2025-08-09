package com.bryan.platform.domain.entity;

import lombok.Data;

/**
 * DeepSeek 消息实体
 *
 * @author Bryan Long
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
