package com.bryan.platform.controller;

import com.bryan.platform.model.Result;
import com.bryan.platform.model.entity.User;
import com.bryan.platform.model.dto.LoginDTO;
import com.bryan.platform.model.dto.UserDTO;
import com.bryan.platform.service.UserService;
import jakarta.validation.Valid; // Jakarta Validation API，用于数据验证
import lombok.RequiredArgsConstructor; // Lombok 注解，自动生成包含 final 字段的构造函数
import org.springframework.http.HttpStatus; // HTTP 状态码
import org.springframework.security.access.prepost.PreAuthorize; // Spring Security 方法级别的权限控制
import org.springframework.validation.annotation.Validated; // 用于触发方法参数的验证
import org.springframework.web.bind.annotation.*; // Spring Web 相关注解

/**
 * ClassName: UserController
 * Package: com.bryan.platform.controller
 * Description: 用户相关的 RESTful API 控制器。
 * 负责处理用户注册、登录、获取当前用户信息、获取指定用户信息等操作。
 * Author: Bryan Long
 * Create: 2025/6/19 - 19:51
 * Version: v1.0
 */
@RestController // 标记为 REST 控制器，返回 JSON 或 XML 数据
@RequestMapping("/api/user") // 设定所有接口的基础路径
@RequiredArgsConstructor // 自动注入 final 声明的 UserService 字段
@Validated // 启用方法参数验证，结合 jakarta.validation.Valid 使用
public class UserController {

    private final UserService userService; // 注入用户服务

    /**
     * 用户注册接口。
     *
     * @param userDTO 用户注册数据传输对象，包含用户名、密码、邮箱等信息。
     * @return 注册成功的用户实体。
     */
    @PostMapping("/register") // 处理 POST 请求，路径为 /api/user/register
    @ResponseStatus(HttpStatus.CREATED) // 设置响应状态码为 201 Created
    public Result<User> register(@RequestBody @Valid UserDTO userDTO) { // @RequestBody 将请求体映射到 userDTO，@Valid 触发验证
        return Result.success(userService.register(userDTO));
    }

    /**
     * 用户登录接口。
     *
     * @param loginDTO 用户登录数据传输对象，包含用户名和密码。
     * @return 登录成功后生成的 JWT Token 字符串。
     */
    @PostMapping("/login") // 处理 POST 请求，路径为 /api/user/login
    public Result<String> login(@RequestBody @Valid LoginDTO loginDTO) { // @RequestBody 将请求体映射到 loginDTO，@Valid 触发验证
        return Result.success(userService.login(loginDTO));
    }

    /**
     * 获取当前认证用户的信息。
     *
     * @return 当前认证用户的实体。
     */
    @GetMapping("/me") // 处理 GET 请求，路径为 /api/user/me
    @PreAuthorize("isAuthenticated()") // 只有已认证（登录）的用户才能访问此接口
    public Result<User> getCurrentUser() {
        return Result.success(userService.getCurrentUser());
    }

    /**
     * 根据用户 ID 获取用户信息。
     * 只有拥有 'ADMIN' 角色的用户才能访问此接口。
     *
     * @param userId 要查询的用户 ID。
     * @return 对应 ID 的用户实体。
     */
    @GetMapping("/{userId}") // 处理 GET 请求，路径为 /api/user/{userId}
    @PreAuthorize("hasRole('ADMIN')") // 只有拥有 ADMIN 角色的用户才能访问
    public Result<User> getUserById(@PathVariable Long userId) { // @PathVariable 从 URL 路径中获取 userId
        return Result.success(userService.getUserById(userId));
    }
}
