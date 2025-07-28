package com.bryan.platform.model.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;

/**
 * 用户注册请求对象
 *
 * @author Bryan Long
 * @since 2025/6/19 - 19:54
 * @version 1.0
 */
@Getter
public class RegisterRequest {
    /**
     * 用户名。
     * 在注册时是必需的。
     */
    @NotBlank(message = "用户名不能为空")
    @Size(min = 2, max = 20, message = "用户名长度应在2-20个字符之间")
    private String username;

    /**
     * 密码。
     * 在注册时是必需的。
     */
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, message = "密码至少6位")
    private String password;

    /**
     * 电话号码。
     * 在注册时是可选的，如果提供则添加，如果不提供则不添加。
     */
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "电话号码格式不正确")
    private String phoneNumber;

    /**
     * 邮箱地址。
     * 在注册时是可选的，如果提供则添加，如果不提供则不添加。
     */
    @Email(message = "邮箱格式不正确")
    private String email;
}
