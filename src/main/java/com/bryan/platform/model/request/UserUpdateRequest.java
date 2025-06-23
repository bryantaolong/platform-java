package com.bryan.platform.model.request;

import jakarta.validation.constraints.Email;
import lombok.Data;

/**
 * ClassName: UserUpdateDTO
 * Package: com.bryan.platform.model.dto
 * Description:
 * Author: Bryan Long
 * Create: 2025/6/21 - 19:37
 * Version: v1.0
 */
@Data
public class UserUpdateRequest {

    /**
     * 用户名。
     * 在更新时是可选的，如果提供则更新，如果不提供则保持不变。
     */
    private String username;

    /**
     * 邮箱。
     * 在更新时是可选的，如果提供则更新。会校验邮箱格式。
     */
    @Email(message = "邮箱格式不正确")
    private String email;
}
