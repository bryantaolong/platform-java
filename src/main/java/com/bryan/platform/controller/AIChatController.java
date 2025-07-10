package com.bryan.platform.controller;

import com.bryan.platform.service.AuthService;
import com.bryan.platform.service.DeepSeekService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.Map;

/**
 * 控制器：AI 聊天接口
 * 提供与 AI 聊天机器人进行对话的接口，接受用户输入并返回 AI 的回复。
 *
 * @author Bryan
 * @version 1.0
 * @since 2025/6/25
 */
@Slf4j
@RestController
@RequestMapping("/api/ai/chat")
@RequiredArgsConstructor
public class AIChatController {

    private final DeepSeekService deepSeekService;
    private final AuthService authService;

    /**
     * 与 AI 进行对话
     *
     * @param payload 请求体，包含用户输入的消息，格式为 {"message": "用户输入"}
     * @return 返回 AI 回复的消息，格式为 {"reply": "AI 回复内容"}
     *
     * @throws IllegalArgumentException 如果消息内容为空或格式不正确
     */
    @PostMapping
    public Map<String, String> chat(@RequestBody Map<String, String> payload) {
        // 1. 获取用户 ID
        Long currentUserId = authService.getCurrentUserId();

        // 2. 从请求体中获取用户输入的消息
        String userMessage = payload.get("message");
        log.info("接收到用户消息: {}", userMessage);

        // 3. 校验消息内容是否为空
        if (userMessage == null || userMessage.trim().isEmpty()) {
            log.warn("消息内容为空");
            throw new IllegalArgumentException("消息内容不能为空");
        }

        // 4. 调用 AI 服务获取回复内容
        String reply = deepSeekService.getChatResponse(currentUserId, userMessage);

        // 5. 封装回复结果返回
        return Collections.singletonMap("reply", reply);
    }

    /**
     * 清空当前用户的聊天上下文
     *
     * @return 返回清空结果提示信息，格式为 {"message": "上下文已清空"}
     */
    @PostMapping("/clear")
    public Map<String, String> clearContext() {
        // 1. 获取当前登录用户 ID
        Long currentUserId = authService.getCurrentUserId();
        log.info("请求清空用户 {} 的上下文", currentUserId);

        // 2. 调用服务清空对应上下文数据
        deepSeekService.clearContext(currentUserId);

        // 3. 返回成功提示信息
        return Collections.singletonMap("message", "上下文已清空");
    }
}
