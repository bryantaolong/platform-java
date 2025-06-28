package com.bryan.platform.controller;

import com.bryan.platform.service.DeepSeekService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.Map;

/**
 * ClassName: ChatController
 * Package: com.bryan.platform.controller
 * Description:
 * Author: Bryan Long
 * Create: 2025/6/25 - 11:18
 * Version: v1.0
 */
@Slf4j
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final DeepSeekService deepSeekService;

    @PostMapping
    @PreAuthorize("true")
    public Map<String, String> chat(@RequestBody Map<String, String> payload) {
        log.info("Received message: {}", payload.get("message"));

        String userMessage = payload.get("message");
        String reply = deepSeekService.getChatResponse(userMessage);
        return Collections.singletonMap("reply", reply);
    }
}
