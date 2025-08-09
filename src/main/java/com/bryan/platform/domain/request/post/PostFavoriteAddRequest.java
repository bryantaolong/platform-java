package com.bryan.platform.domain.request.post;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * PostFavoriteAddRequest
 *
 * @author Bryan Long
 */
@Data
public class PostFavoriteAddRequest {
    @NotBlank(message = "博文ID不能为空")
    private String postId;
}
