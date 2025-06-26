package com.bryan.platform.model.response;

import com.bryan.platform.model.entity.DeepSeekMessage;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * ClassName: DeepSeekResponse
 * Package: com.bryan.platform.model.response
 * Description:
 * Author: Bryan Long
 * Create: 2025/6/25 - 11:12
 * Version: v1.0
 */
@Data
public class DeepSeekResponse {
    private List<Choice> choices;

    public String getFirstReply() {
        return choices.get(0).getMessage().getContent();
    }
}

@Getter
@Setter
class Choice {
    private DeepSeekMessage message;

    // getters and setters
}
