package com.bryan.platform.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bryan.platform.model.Result;
import com.bryan.platform.model.dto.UserLoginDTO;
import com.bryan.platform.model.dto.UserUpdateDTO;
import com.bryan.platform.model.entity.User;
import com.bryan.platform.model.dto.UserRegisterDTO;
import com.bryan.platform.model.dto.ChangePasswordDTO;
import com.bryan.platform.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * ClassName: UserController
 * Package: com.bryan.platform.controller
 * Description: 用户相关的 RESTful API 控制器。
 * 负责处理用户注册、登录、信息获取、更新、权限变更、密码修改和删除等操作。
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
     * @param userRegisterDTO 用户注册数据传输对象，包含用户名、密码、邮箱等信息。
     * @return 注册成功的用户实体。
     */
    @PostMapping("/register") // 处理 POST 请求，路径为 /api/user/register
    @ResponseStatus(HttpStatus.CREATED) // 设置响应状态码为 201 Created
    public Result<User> register(@RequestBody @Valid UserRegisterDTO userRegisterDTO) { // @RequestBody 将请求体映射到 userDTO，@Valid 触发验证
        return Result.success(userService.register(userRegisterDTO));
    }

    /**
     * 用户登录接口。
     *
     * @param userLoginDTO 用户登录数据传输对象，包含用户名和密码。
     * @return 登录成功后生成的 JWT Token 字符串。
     */
    @PostMapping("/login") // 处理 POST 请求，路径为 /api/user/login
    public Result<String> login(@RequestBody @Valid UserLoginDTO userLoginDTO) { // @RequestBody 将请求体映射到 loginDTO，@Valid 触发验证
        return Result.success(userService.login(userLoginDTO));
    }

    /**
     * 获取所有用户列表。
     * 此接口通常需要 'ADMIN' 角色权限才能访问。
     * 注意：由于 UserService 接口的 getAllUsers() 没有分页参数，这里直接调用服务层获取所有用户。
     * 在实际项目中，建议在服务层和控制器层都添加分页参数。
     *
     * @return 包含所有用户数据的分页对象。
     */
    @GetMapping("/all") // 处理 GET 请求，路径为 /api/user/all
    @PreAuthorize("hasRole('ADMIN')") // 只有拥有 ADMIN 角色的用户才能访问
    public Result<Page<User>> getAllUsers() {
        return Result.success(userService.getAllUsers());
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

    /**
     * 根据用户名获取用户信息。
     * 此接口通常需要 'ADMIN' 角色权限才能访问。
     *
     * @param username 要查询的用户名。
     * @return 对应用户名的用户实体。
     */
    @GetMapping("/username/{username}") // 处理 GET 请求，路径为 /api/user/username/{username}
    @PreAuthorize("hasRole('ADMIN')") // 只有拥有 ADMIN 角色的用户才能访问
    public Result<User> getUserByUsername(@PathVariable String username) { // @PathVariable 从 URL 路径中获取 username
        return Result.success(userService.getUserByUsername(username));
    }

    /**
     * 更新用户基本信息。
     * 允许用户更新自己的信息，或由管理员更新任意用户信息。
     *
     * @param userId  要更新的用户 ID。
     * @param userUpdateDTO 包含需要更新的用户信息（用户名、邮箱）。
     * @return 更新后的用户实体。
     */
    @PutMapping("/{userId}") // 处理 PUT 请求，路径为 /api/user/{userId}
    @PreAuthorize("hasRole('ADMIN') or (#userId == authentication.principal.id)")
    // 要求 ADMIN 角色，或当前认证用户ID与路径中的 userId 相同
    // 注意: authentication.principal 可能需要类型转换才能获取 id。
    // 如果 authentication.principal 是 UserDetails 类型，且 UserDetails 的 username 是 userId 的字符串形式，
    // 则可以使用 @PreAuthorize("hasRole('ADMIN') or (#userId == authentication.name)")
    // 但鉴于我们之前为了 getId() 进行了类型转换，此处可能需要更复杂的 SpEL 表达式。
    // 最简化的方式是：@PreAuthorize("hasRole('ADMIN') or (isAuthenticated() and #userId.toString() == authentication.principal.id.toString())")
    // 假设 UserDetails 实际就是你的 User 实体，且其 getId() 可用
    public Result<User> updateUser(
            @PathVariable Long userId,
            @RequestBody @Valid UserUpdateDTO userUpdateDTO) {
        return Result.success(userService.updateUser(userId, userUpdateDTO));
    }

    /**
     * 更改用户角色。
     * 只有拥有 'ADMIN' 角色的用户才能执行此操作。
     *
     * @param userId 要更改角色的用户 ID。
     * @param roles  新的角色字符串，多个角色以逗号分隔（例如 "ROLE_USER,ROLE_ADMIN"）。
     * @return 更新后的用户实体。
     */
    @PutMapping("/{userId}/role") // 处理 PUT 请求，路径为 /api/user/{userId}/role
    @PreAuthorize("hasRole('ADMIN')") // 只有拥有 ADMIN 角色的用户才能访问
    public Result<User> changeRole(
            @PathVariable Long userId,
            @RequestBody String roles) { // 直接接收 String 类型的角色字符串
        return Result.success(userService.changeRole(userId, roles));
    }

    /**
     * 更改用户密码。
     * 允许用户更改自己的密码，或由管理员更改任意用户密码。
     *
     * @param userId            要更改密码的用户 ID。
     * @param changePasswordDTO 包含旧密码和新密码的数据传输对象。
     * @return 更新后的用户实体。
     */
    @PutMapping("/{userId}/password") // 处理 PUT 请求，路径为 /api/user/{userId}/password
    @PreAuthorize("hasRole('ADMIN') or (#userId == authentication.principal.id)")
    public Result<User> changePassword(
            @PathVariable Long userId,
            @RequestBody @Valid ChangePasswordDTO changePasswordDTO) {
        return Result.success(userService.changePassword(
                userId,
                changePasswordDTO.getOldPassword(),
                changePasswordDTO.getNewPassword()
        ));
    }

    /**
     * 删除用户（逻辑删除）。
     * 只有拥有 'ADMIN' 角色的用户才能执行此操作。
     *
     * @param userId 要删除的用户 ID。
     * @return 被删除的用户实体。
     */
    @DeleteMapping("/{userId}") // 处理 DELETE 请求，路径为 /api/user/{userId}
    @PreAuthorize("hasRole('ADMIN')") // 只有拥有 ADMIN 角色的用户才能访问
    public Result<User> deleteUser(@PathVariable Long userId) {
        return Result.success(userService.deleteUser(userId));
    }
}
