package com.bryan.platform.model.request;

import com.bryan.platform.model.entity.DeepSeekMessage;
import lombok.Data;

import java.util.List;

/**
 * ClassName: DeepSeekRequest
 * Package: com.bryan.platform.model.request
 * Description:
 * Author: Bryan Long
 * Create: 2025/6/25 - 11:11
 * Version: v1.0
 */
@Data
public class DeepSeekRequest {

    private String model;

    private List<DeepSeekMessage> messages;
}