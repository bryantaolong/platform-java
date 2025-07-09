package com.bryan.platform.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bryan.platform.model.request.UserExportRequest;
import com.bryan.platform.model.response.Result;
import com.bryan.platform.model.request.UserUpdateRequest;
import com.bryan.platform.model.entity.User;
import com.bryan.platform.model.request.ChangePasswordRequest;
import com.bryan.platform.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

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

    /**
     * 导出用户数据，支持字段选择。
     * <p>仅管理员可执行。</p>
     *
     * @param response HTTP 响应对象，用于写出导出文件
     * @param request  导出请求体，包含导出字段和筛选条件
     */
    @PostMapping("/export")
    @PreAuthorize("hasRole('ADMIN')")
    public void exportUsers(HttpServletResponse response,
                            @RequestBody UserExportRequest request) {
        // 1. 调用服务执行导出
        userService.exportUsers(response, request);
    }

    /**
     * 导出所有用户数据，包含所有字段。
     * <p>仅管理员可执行。</p>
     *
     * @param response HTTP 响应对象
     * @param status   用户状态筛选，非必填
     * @param fileName 导出文件名，默认 "用户数据"
     */
    @GetMapping("/export/all")
    @PreAuthorize("hasRole('ADMIN')")
    public void exportAllUsers(HttpServletResponse response,
                               @RequestParam(required = false) Integer status,
                               @RequestParam(required = false, defaultValue = "用户数据") String fileName) {
        // 1. 调用服务执行导出
        userService.exportAllUsers(response, status, fileName);
    }

    /**
     * 获取可供导出的字段列表，供前端动态选择。
     * <p>仅管理员可访问。</p>
     *
     * @return 字段名与中文描述的映射表
     */
    @GetMapping("/export/fields")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Map<String, String>> getExportFields() {
        // 1. 准备字段映射
        Map<String, String> fields = new LinkedHashMap<>();
        fields.put("id", "用户ID");
        fields.put("username", "用户名");
        fields.put("email", "邮箱");
        fields.put("roles", "角色");
        fields.put("statusText", "状态");
        fields.put("createTime", "创建时间");
        fields.put("updateTime", "更新时间");

        // 2. 返回字段映射结果
        return Result.success(fields);
    }
}
