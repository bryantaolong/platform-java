package com.bryan.platform.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bryan.platform.model.response.Result;
import com.bryan.platform.model.request.LoginRequest;
import com.bryan.platform.model.request.UserUpdateRequest;
import com.bryan.platform.model.entity.User;
import com.bryan.platform.model.request.RegisterRequest;
import com.bryan.platform.model.request.ChangePasswordRequest;
import com.bryan.platform.service.AuthService;
import com.bryan.platform.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * ClassName: AuthController
 * Package: com.bryan.platform.controller
 * Description:
 * Author: Bryan Long
 * Create: 2025/6/28 - 14:18
 * Version: v1.0
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Validated
public class AuthController {

    private final AuthService authService; // 注入用户服务

    /**
     * 用户注册接口。
     *
     * @param registerRequest 用户注册数据传输对象，包含用户名、密码、邮箱等信息。
     * @return 注册成功的用户实体。
     */
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED) // 设置响应状态码为 201 Created
    public Result<User> register(@RequestBody @Valid RegisterRequest registerRequest) {
        return Result.success(authService.register(registerRequest));
    }

    /**
     * 用户登录接口。
     *
     * @param loginRequest 用户登录数据传输对象，包含用户名和密码。
     * @return 登录成功后生成的 JWT Token 字符串。
     */
    @PostMapping("/login")
    public Result<String> login(@RequestBody @Valid LoginRequest loginRequest) {
        return Result.success(authService.login(loginRequest));
    }

    /**
     * 获取当前认证用户的信息。
     *
     * @return 当前认证用户的实体。
     */
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public Result<User> getCurrentUser() {
        return Result.success(authService.getCurrentUser());
    }
}
