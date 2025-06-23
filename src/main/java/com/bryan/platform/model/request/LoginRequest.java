package com.bryan.platform.model.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ClassName: LoginDTO
 * Package: com.bryan.platform.entity.dto
 * Description:
 * Author: Bryan Long
 * Create: 2025/6/19 - 19:59
 * Version: v1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {
    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;
}
