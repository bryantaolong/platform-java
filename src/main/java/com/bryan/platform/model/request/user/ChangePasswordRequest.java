package com.bryan.platform.model.request.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;

/**
 * 密码修改请求对象
 * 用于封装用户修改密码时需要提供的旧密码和新密码信息
 *
 * @author Bryan Long
 * @since 2025/6/21-19:06
 * @version 1.0
 */
@Getter
public class ChangePasswordRequest {
    /**
     * 旧密码。
     * 不能为空，用于验证用户身份。
     */
    @NotBlank(message = "旧密码不能为空") // 验证注解：确保字段不为 null 且不为空白字符串
    @Size(min = 6, message = "密码至少6位")
    private String oldPassword;

    /**
     * 新密码。
     * 不能为空，且通常需要符合一定的密码复杂度要求（此处仅为非空校验）。
     */
    @NotBlank(message = "新密码不能为空") // 验证注解：确保字段不为 null 且不为空白字符串
    @Size(min = 6, message = "密码至少6位")
    private String newPassword;
}
