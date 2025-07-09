package com.bryan.platform.service;

import com.bryan.platform.config.properties.DeepSeekApiProperties;
import com.bryan.platform.model.entity.DeepSeekMessage;
import com.bryan.platform.model.request.DeepSeekRequest;
import com.bryan.platform.model.response.DeepSeekResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;

/**
 * DeepSeekService 业务服务类
 * 负责调用 DeepSeek API 实现聊天回复功能
 *
 * @author Bryan Long
 * @version 1.0
 * @since 2025/6/25
 */
@Service
@RequiredArgsConstructor
public class DeepSeekService {

    private final DeepSeekApiProperties properties;
    private final RestTemplate restTemplate;

    /**
     * 根据用户输入消息，调用 DeepSeek API 获取聊天回复内容
     *
     * @param userMessage 用户发送的消息文本，不能为空
     * @return DeepSeek API 返回的聊天回复文本
     * @throws RestClientException 当调用远程 API 发生网络或响应错误时抛出
     */
    public String getChatResponse(String userMessage) throws RestClientException {
        // 1. 构建请求对象，设置模型及用户消息
        DeepSeekRequest request = new DeepSeekRequest();
        request.setModel(properties.getModel());
        request.setMessages(Collections.singletonList(
                new DeepSeekMessage("user", userMessage)
        ));

        // 2. 构建请求头，设置内容类型为 JSON 并添加认证信息
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + properties.getKey());

        // 3. 封装请求实体，包括请求体和请求头
        HttpEntity<DeepSeekRequest> httpEntity = new HttpEntity<>(request, headers);

        // 4. 发送 POST 请求调用 DeepSeek API，接收响应实体
        ResponseEntity<DeepSeekResponse> response = restTemplate.postForEntity(
                properties.getUrl(),
                httpEntity,
                DeepSeekResponse.class
        );

        // 5. 从响应体中提取并返回第一条回复内容
        assert response.getBody() != null;
        return response.getBody().getFirstReply();
    }
}
