package com.bryan.platform.model.request;

import jakarta.validation.constraints.Email;
import lombok.Getter;

/**
 * 用户更新请求对象
 *
 * @author Bryan Long
 * @since 2025/6/21 - 19:37
 * @version 1.0
 */
@Getter
public class UserUpdateRequest {
    /**
     * 用户名。
     * 在更新时是可选的，如果提供则更新，如果不提供则保持不变。
     */
    private String username;

    private String phoneNumber;

    /**
     * 邮箱。
     * 在更新时是可选的，如果提供则更新。会校验邮箱格式。
     */
    @Email(message = "邮箱格式不正确")
    private String email;

    private Integer gender;

    private String avatar;
}
