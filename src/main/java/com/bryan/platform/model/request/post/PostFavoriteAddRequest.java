package com.bryan.platform.model.request.post;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * PostFavoriteAddRequest
 *
 * @author Bryan Long
 * @since 2025/6/22 - 16:19
 * @version 1.0
 */
@Data
public class PostFavoriteAddRequest {
    @NotBlank(message = "博文ID不能为空")
    private String postId;
}
