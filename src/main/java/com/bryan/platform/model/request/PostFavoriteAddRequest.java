package com.bryan.platform.model.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * ClassName: FavoriteUpdateDTO
 * Package: com.bryan.platform.model.dto
 * Description:
 * Author: Bryan Long
 * Create: 2025/6/22 - 16:19
 * Version: v1.0
 */
@Data
public class PostFavoriteAddRequest {
    @NotBlank(message = "博文ID不能为空")
    private String postId;
}
