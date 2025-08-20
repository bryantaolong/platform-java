package com.bryan.platform.controller;

import com.bryan.platform.domain.entity.user.SysUser;
import com.bryan.platform.domain.response.Result;
import com.bryan.platform.domain.request.auth.LoginRequest;
import com.bryan.platform.domain.request.auth.RegisterRequest;
import com.bryan.platform.service.user.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 控制器：认证与授权接口
 * 提供用户注册、登录、当前用户信息获取及 Token 校验等接口。
 *
 * @author Bryan Long
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Validated
public class AuthController {

    private final AuthService authService;

    /**
     * 用户注册接口
     *
     * @param registerRequest 用户注册信息，包括用户名、密码、邮箱等
     * @return 注册成功的用户对象封装在统一响应结构中
     */
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED) // 设置 HTTP 状态码为 201 Created
    public Result<SysUser> register(@RequestBody @Valid RegisterRequest registerRequest) {
        // 1. 调用注册服务，创建新用户
        SysUser user = authService.register(registerRequest);

        // 2. 返回注册结果
        return Result.success(user);
    }

    /**
     * 用户登录接口
     *
     * @param loginRequest 用户登录信息，包括用户名和密码
     * @return 登录成功后生成的 JWT Token 字符串封装在统一响应结构中
     */
    @PostMapping("/login")
    public Result<String> login(@RequestBody @Valid LoginRequest loginRequest) {
        // 1. 调用认证服务进行登录验证
        String token = authService.login(loginRequest);

        // 2. 返回生成的 JWT Token
        return Result.success(token);
    }

    /**
     * 获取当前认证用户信息
     *
     * @return 当前登录用户的 User 实体对象封装在统一响应结构中
     */
    @GetMapping("/me")
    public Result<SysUser> getCurrentUser() {
        // 1. 从 Spring Security 上下文中获取当前登录用户
        SysUser currentUser = authService.getCurrentUser();

        // 2. 返回用户信息
        return Result.success(currentUser);
    }

    /**
     * 退出登录
     *
     * @return boolean 是否退出登录
     */
    @GetMapping("/logout")
    public boolean logout() {
        return authService.logout();
    }

    /**
     * 验证 Token 合法性及用户状态
     *
     * @param token JWT Token 字符串
     * @param userDetails 当前已认证的用户信息（通过 Spring Security 注入）
     * @return 验证结果字符串封装在统一响应结构中
     */
    @GetMapping("/validate")
    public Result<String> validate(
            @RequestParam String token,
            @AuthenticationPrincipal UserDetails userDetails) {
        // 1. 验证 Token 格式与签名是否合法
        if (!authService.validateToken(token)) {
            return Result.success("Invalid token");
        }

        // 2. 校验用户账户状态（仅在已认证情况下执行）
        if (userDetails != null) {
            // 2.1 检查账户是否被锁定
            if (!userDetails.isAccountNonLocked()) {
                return Result.success("Account locked");
            }

            // 2.2 检查账户是否过期
            if (!userDetails.isAccountNonExpired()) {
                return Result.success("Account expired");
            }

            // 2.3 检查账户是否被禁用
            if (!userDetails.isEnabled()) {
                return Result.success("Account disabled");
            }
        }

        // 3. 校验通过
        return Result.success("Validation passed");
    }
}
