package com.bryan.platform.domain.request;

import com.bryan.platform.domain.entity.DeepSeekMessage;
import lombok.Data;

import java.util.List;

/**
 * DeepSeek API 请求对象
 *
 * @author Bryan Long
 */
@Data
public class DeepSeekRequest {
    private String model;

    private List<DeepSeekMessage> messages;
}
