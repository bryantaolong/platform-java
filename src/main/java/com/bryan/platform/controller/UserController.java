package com.bryan.platform.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bryan.platform.model.request.PageRequest;
import com.bryan.platform.model.request.UserSearchRequest;
import com.bryan.platform.model.response.Result;
import com.bryan.platform.model.request.UserUpdateRequest;
import com.bryan.platform.model.entity.User;
import com.bryan.platform.model.request.ChangePasswordRequest;
import com.bryan.platform.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 用户控制器：提供用户相关的 RESTful API 接口。
 * 包括用户信息查询、更新、角色变更、密码修改、逻辑删除及用户数据导出等功能。
 * 依赖 Spring Security 进行权限控制，支持管理员和普通用户不同权限的接口访问。
 *
 * @author Bryan
 * @version 1.0
 * @since 2025/6/19
 */
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@Validated
public class UserController {

    private final UserService userService;

    /**
     * 获取所有用户列表（不分页）。
     * <p>仅允许拥有 ADMIN 角色的用户访问。</p>
     *
     * @return 包含所有用户数据的分页对象（目前不支持分页参数，建议后续优化）。
     */
    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Page<User>> getAllUsers() {
        // 1. 调用服务层获取所有用户列表
        return Result.success(userService.getAllUsers());
    }

    /**
     * 根据用户 ID 查询用户信息。
     * <p>仅允许 ADMIN 角色访问。</p>
     *
     * @param userId 目标用户ID
     * @return 对应用户实体
     */
    @GetMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<User> getUserById(@PathVariable Long userId) {
        // 1. 调用服务获取用户信息
        return Result.success(userService.getUserById(userId));
    }

    /**
     * 根据用户名查询用户信息。
     * <p>仅允许 ADMIN 角色访问。</p>
     *
     * @param username 用户名
     * @return 对应用户实体
     */
    @GetMapping("/username/{username}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<User> getUserByUsername(@PathVariable String username) {
        // 1. 调用服务获取用户信息
        return Result.success(userService.getUserByUsername(username));
    }

    /**
     * 用户搜索接口，支持多条件模糊查询和分页。
     *
     * @param searchRequest 搜索条件
     * @param pageRequest   分页参数
     * @return 用户分页结果
     */
    @PostMapping("/search")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Page<User>> searchUsers(
            @RequestBody UserSearchRequest searchRequest,
            @ModelAttribute PageRequest pageRequest) {
        Page<User> page = userService.searchUsers(searchRequest, pageRequest);
        return Result.success(page);
    }

    /**
     * 更新用户基本信息。
     * <p>允许管理员更新任意用户信息，或用户本人更新自己的信息。</p>
     *
     * @param userId             目标用户ID
     * @param userUpdateRequest  包含需要更新的信息（用户名、邮箱等）
     * @return 更新后的用户实体
     * @throws IllegalArgumentException 当权限校验失败时抛出
     */
    @PutMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN') or (#userId == authentication.principal.id)")
    public Result<User> updateUser(
            @PathVariable Long userId,
            @RequestBody @Valid UserUpdateRequest userUpdateRequest) {
        // 1. 调用服务更新用户信息
        return Result.success(userService.updateUser(userId, userUpdateRequest));
    }

    /**
     * 修改用户角色。
     * <p>仅管理员可操作。</p>
     *
     * @param userId 目标用户ID
     * @param roles  新角色字符串，逗号分隔（如 "ROLE_USER,ROLE_ADMIN"）
     * @return 更新后的用户实体
     */
    @PutMapping("/{userId}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<User> changeRole(
            @PathVariable Long userId,
            @RequestBody String roles) {
        // 1. 调用服务变更角色
        return Result.success(userService.changeRole(userId, roles));
    }

    /**
     * 修改用户密码。
     * <p>管理员可修改任意用户密码，用户本人可修改自己的密码。</p>
     *
     * @param userId               目标用户ID
     * @param changePasswordRequest 包含旧密码和新密码的请求体
     * @return 更新后的用户实体
     */
    @PutMapping("/{userId}/password")
    @PreAuthorize("hasRole('ADMIN') or (#userId == authentication.principal.id)")
    public Result<User> changePassword(
            @PathVariable Long userId,
            @RequestBody @Valid ChangePasswordRequest changePasswordRequest) {
        // 1. 调用服务层执行密码修改
        return Result.success(userService.changePassword(
                userId,
                changePasswordRequest.getOldPassword(),
                changePasswordRequest.getNewPassword()
        ));
    }

    /**
     * 封禁用户。
     * <p>仅管理员可操作。</p>
     *
     * @param userId 目标用户ID
     * @return 更新后的用户实体
     */
    @PutMapping("/{userId}/block")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<User> blockUser(
            @PathVariable Long userId) {
        // 1. 调用服务封禁用户
        return Result.success(userService.blockUser(userId));
    }

    /**
     * 解封用户。
     * <p>仅管理员可操作。</p>
     *
     * @param userId 目标用户ID
     * @return 更新后的用户实体
     */
    @PutMapping("/{userId}/unblock")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<User> unblockUser(
            @PathVariable Long userId) {
        // 1. 调用服务解封用户
        return Result.success(userService.unblockUser(userId));
    }

    /**
     * 删除用户（逻辑删除）。
     * <p>仅管理员可执行。</p>
     *
     * @param userId 目标用户ID
     * @return 被删除的用户实体
     */
    @DeleteMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<User> deleteUser(@PathVariable Long userId) {
        // 1. 调用服务执行逻辑删除
        return Result.success(userService.deleteUser(userId));
    }
}
