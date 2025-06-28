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
 * ClassName: UserController
 * Package: com.bryan.platform.controller
 * Description: 用户相关的 RESTful API 控制器。
 * 负责处理用户注册、登录、信息获取、更新、权限变更、密码修改和删除等操作。
 * Author: Bryan Long
 * Create: 2025/6/19 - 19:51
 * Version: v1.0
 */
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@Validated
public class UserController {

    private final UserService userService; // 注入用户服务

    /**
     * 获取所有用户列表。
     * 此接口通常需要 'ADMIN' 角色权限才能访问。
     * 注意：由于 UserService 接口的 getAllUsers() 没有分页参数，这里直接调用服务层获取所有用户。
     * 在实际项目中，建议在服务层和控制器层都添加分页参数。
     *
     * @return 包含所有用户数据的分页对象。
     */
    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Page<User>> getAllUsers() {
        return Result.success(userService.getAllUsers());
    }

    /**
     * 根据用户 ID 获取用户信息。
     * 只有拥有 'ADMIN' 角色的用户才能访问此接口。
     *
     * @param userId 要查询的用户 ID。
     * @return 对应 ID 的用户实体。
     */
    @GetMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<User> getUserById(@PathVariable Long userId) {
        return Result.success(userService.getUserById(userId));
    }

    /**
     * 根据用户名获取用户信息。
     * 此接口通常需要 'ADMIN' 角色权限才能访问。
     *
     * @param username 要查询的用户名。
     * @return 对应用户名的用户实体。
     */
    @GetMapping("/username/{username}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<User> getUserByUsername(@PathVariable String username) {
        return Result.success(userService.getUserByUsername(username));
    }

    /**
     * 更新用户基本信息。
     * 允许用户更新自己的信息，或由管理员更新任意用户信息。
     *
     * @param userId  要更新的用户 ID。
     * @param userUpdateRequest 包含需要更新的用户信息（用户名、邮箱）。
     * @return 更新后的用户实体。
     */
    @PutMapping("/{userId}")
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
            @RequestBody @Valid UserUpdateRequest userUpdateRequest) {
        return Result.success(userService.updateUser(userId, userUpdateRequest));
    }

    /**
     * 更改用户角色。
     * 只有拥有 'ADMIN' 角色的用户才能执行此操作。
     *
     * @param userId 要更改角色的用户 ID。
     * @param roles  新的角色字符串，多个角色以逗号分隔（例如 "ROLE_USER,ROLE_ADMIN"）。
     * @return 更新后的用户实体。
     */
    @PutMapping("/{userId}/role")
    @PreAuthorize("hasRole('ADMIN')")
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
     * @param changePasswordRequest 包含旧密码和新密码的数据传输对象。
     * @return 更新后的用户实体。
     */
    @PutMapping("/{userId}/password")
    @PreAuthorize("hasRole('ADMIN') or (#userId == authentication.principal.id)")
    public Result<User> changePassword(
            @PathVariable Long userId,
            @RequestBody @Valid ChangePasswordRequest changePasswordRequest) {
        return Result.success(userService.changePassword(
                userId,
                changePasswordRequest.getOldPassword(),
                changePasswordRequest.getNewPassword()
        ));
    }

    /**
     * 删除用户（逻辑删除）。
     * 只有拥有 'ADMIN' 角色的用户才能执行此操作。
     *
     * @param userId 要删除的用户 ID。
     * @return 被删除的用户实体。
     */
    @DeleteMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<User> deleteUser(@PathVariable Long userId) {
        return Result.success(userService.deleteUser(userId));
    }

    /**
     * 导出用户数据（支持字段选择）
     * 只有管理员可以导出用户数据
     */
    @PostMapping("/export")
    @PreAuthorize("hasRole('ADMIN')")
    public void exportUsers(HttpServletResponse response,
                            @RequestBody UserExportRequest request) {
        userService.exportUsers(response, request);
    }

    /**
     * 导出所有用户数据（包含所有字段）
     * 只有管理员可以导出用户数据
     */
    @GetMapping("/export/all")
    @PreAuthorize("hasRole('ADMIN')")
    public void exportAllUsers(HttpServletResponse response,
                               @RequestParam(required = false) Integer status,
                               @RequestParam(required = false, defaultValue = "用户数据") String fileName) {
        userService.exportAllUsers(response, status, fileName);
    }

    /**
     * 获取可导出的字段列表（供前端选择）
     */
    @GetMapping("/export/fields")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Map<String, String>> getExportFields() {
        Map<String, String> fields = new LinkedHashMap<>();
        fields.put("id", "用户ID");
        fields.put("username", "用户名");
        fields.put("email", "邮箱");
        fields.put("roles", "角色");
        fields.put("statusText", "状态");
        fields.put("createTime", "创建时间");
        fields.put("updateTime", "更新时间");
        return Result.success(fields);
    }
}
