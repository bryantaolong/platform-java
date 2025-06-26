package com.bryan.platform.service;

import com.bryan.platform.config.properties.DeepSeekApiProperties;
import com.bryan.platform.model.entity.DeepSeekMessage;
import com.bryan.platform.model.request.DeepSeekRequest;
import com.bryan.platform.model.response.DeepSeekResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

/**
 * ClassName: DeepSeekService
 * Package: com.bryan.platform.service
 * Description:
 * Author: Bryan Long
 * Create: 2025/6/25 - 11:16
 * Version: v1.0
 */
@Service
@RequiredArgsConstructor
public class DeepSeekService {

    private final DeepSeekApiProperties properties;

    private final RestTemplate restTemplate;

    public String getChatResponse(String userMessage) {
        DeepSeekRequest request = new DeepSeekRequest();
        request.setModel(properties.getModel());
        request.setMessages(Collections.singletonList(
                new DeepSeekMessage("user", userMessage)
        ));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + properties.getKey());

        HttpEntity<DeepSeekRequest> httpEntity = new HttpEntity<>(request, headers);

        ResponseEntity<DeepSeekResponse> response = restTemplate.postForEntity(
                properties.getUrl(),
                httpEntity,
                DeepSeekResponse.class
        );

        return response.getBody().getFirstReply();
    }
}
