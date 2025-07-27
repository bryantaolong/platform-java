package com.bryan.platform.model.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

/**
 * 登录请求对象
 *
 * @author Bryan Long
 * @since 2025/6/19 - 19:59
 * @version 1.0
 */
@Getter
public class LoginRequest {
    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;
}
