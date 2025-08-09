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

import java.util.*;

/**
 * DeepSeekService 业务服务类
 * 负责调用 DeepSeek API 实现聊天回复功能
 *
 * @author Bryan Long
 */
@Service
@RequiredArgsConstructor
public class DeepSeekService {

    private final DeepSeekApiProperties properties;
    private final RestTemplate restTemplate;

    // 用于保存每个用户的上下文对话（注意：生产环境应使用缓存或数据库）
    private final Map<Long, List<DeepSeekMessage>> userContextMap = new HashMap<>();

    private static final int MAX_CONTEXT_SIZE = 20;

    /**
     * 与 AI 进行对话，包含上下文记忆
     *
     * @param userId 用户 ID，用于区分不同用户会话
     * @param userMessage 用户发送的消息文本
     * @return AI 返回的回复内容
     * @throws RestClientException 调用远程 API 异常
     */
    public String getChatResponse(Long userId, String userMessage) throws RestClientException {
        // 1. 获取用户历史消息上下文
        List<DeepSeekMessage> context = userContextMap.computeIfAbsent(userId, k -> new ArrayList<>());

        // 2. 添加当前用户消息
        context.add(new DeepSeekMessage("user", userMessage));

        // 3. 构建请求体
        DeepSeekRequest request = new DeepSeekRequest();
        request.setModel(properties.getModel());
        request.setMessages(trimContext(context)); // 控制上下文数量

        // 4. 设置请求头
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + properties.getKey());

        HttpEntity<DeepSeekRequest> httpEntity = new HttpEntity<>(request, headers);

        // 5. 调用远程 API
        ResponseEntity<DeepSeekResponse> response = restTemplate.postForEntity(
                properties.getUrl(),
                httpEntity,
                DeepSeekResponse.class
        );

        // 6. 记录 AI 回复并返回
        String reply = Objects.requireNonNull(response.getBody()).getFirstReply();
        context.add(new DeepSeekMessage("assistant", reply));
        return reply;
    }

    /**
     * 清空指定用户的上下文（可提供给控制器或前端手动清空上下文）
     *
     * @param userId 用户 ID
     */
    public void clearContext(Long userId) {
        userContextMap.remove(userId);
    }

    /**
     * 保持上下文消息条数在限制范围内
     */
    private List<DeepSeekMessage> trimContext(List<DeepSeekMessage> context) {
        int start = Math.max(0, context.size() - MAX_CONTEXT_SIZE);
        return context.subList(start, context.size());
    }
}
