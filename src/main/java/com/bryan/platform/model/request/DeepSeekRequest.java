package com.bryan.platform.model.request;

import com.bryan.platform.model.entity.DeepSeekMessage;
import lombok.Data;

import java.util.List;

/**
 * DeepSeek API 请求对象
 *
 * @author Bryan Long
 * @since 2025/6/25 - 11:11
 * @version 1.0
 */
@Data
public class DeepSeekRequest {
    private String model;

    private List<DeepSeekMessage> messages;
}