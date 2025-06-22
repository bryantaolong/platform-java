package com.bryan.platform.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bryan.platform.common.exception.BusinessException;
import com.bryan.platform.common.exception.ResourceNotFoundException;
import com.bryan.platform.model.dto.UserLoginDTO;
import com.bryan.platform.model.dto.UserUpdateDTO;
import com.bryan.platform.model.entity.User;
import com.bryan.platform.model.dto.UserRegisterDTO;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

/**
 * ClassName: UserService
 * Package: com.bryan.platform.service
 * Description: 用户服务接口。
 * 定义了用户相关的核心业务操作，包括用户注册、登录、信息查询等。
 * 同时，它也扩展了 Spring Security 的 UserDetailsService 职责，用于加载用户详情。
 * Author: Bryan Long
 * Create: 2025/6/19 - 19:49
 * Version: v1.0
 */
public interface UserService {
    /**
     * 注册新用户。
     *
     * @param userRegisterDTO 包含注册用户信息的DTO。
     * @return 注册成功的用户实体。
     */
    User register(UserRegisterDTO userRegisterDTO);

    /**
     * 用户登录。
     *
     * @param userLoginDTO 包含用户登录凭据的DTO（用户名和密码）。
     * @return 登录成功后生成的JWT Token字符串。
     */
    String login(UserLoginDTO userLoginDTO);

    /**
     * 获取当前已认证的用户信息。
     *
     * @return 当前用户的实体。
     */
    User getCurrentUser();

    /**
     * 获取所有用户列表（支持分页，但此处由于接口未提供 Pageable，默认返回所有用户）。
     *
     * @return 包含所有用户数据的 Page 对象。
     */
    Page<User> getAllUsers();

    /**
     * 根据用户ID获取用户实体。
     *
     * @param userId 要查询的用户ID。
     * @return 对应ID的用户实体。
     */
    User getUserById(Long userId);

    /**
     * 根据用户名获取用户实体。
     * 此方法现在是 UserService 接口的显式实现。
     *
     * @param username 用户名。
     * @return 对应的用户实体，如果不存在则返回 null。
     */
    User getUserByUsername(String username);

    /**
     * 更新用户基本信息。
     *
     * @param userId  要更新的用户ID。
     * @param userUpdaterDTO 包含需要更新的用户信息（用户名、邮箱）。密码和角色修改请使用专门方法。
     * @return 更新后的用户实体。
     * @throws ResourceNotFoundException 如果用户不存在。
     * @throws BusinessException 如果尝试更新的用户名已存在（且不是当前用户自身）。
     */
    User updateUser(Long userId, UserUpdateDTO userUpdaterDTO);

    /**
     * 更改用户角色。
     * 此操作通常需要管理员权限。
     *
     * @param userId 要更改角色的用户ID。
     * @param roles  新的角色字符串，多个角色以逗号分隔（例如 "ROLE_USER,ROLE_ADMIN"）。
     * @return 更新后的用户实体。
     * @throws ResourceNotFoundException 如果用户不存在。
     */
    User changeRole(Long userId, String roles);

    /**
     * 更改用户密码。
     *
     * @param userId      要更改密码的用户ID。
     * @param oldPassword 旧密码（明文）。
     * @param newPassword 新密码（明文）。
     * @return 更新后的用户实体。
     * @throws ResourceNotFoundException 如果用户不存在。
     * @throws BusinessException 如果旧密码不正确。
     */
    User changePassword(Long userId, String oldPassword, String newPassword);

    /**
     * 封禁用户。
     * 此操作通常需要管理员权限。
     *
     * @param userId 要更改角色的用户ID。
     * @return 更新后的用户实体。
     * @throws ResourceNotFoundException 如果用户不存在。
     */
    User blockUser(Long userId);

    /**
     * 解封用户。
     * 此操作通常需要管理员权限。
     *
     * @param userId 要更改角色的用户ID。
     * @return 更新后的用户实体。
     * @throws ResourceNotFoundException 如果用户不存在。
     */
    User unblockUser(Long userId);

    /**
     * 删除用户（逻辑删除）。
     * 假设 User 实体配置了 @TableLogic 注解，此方法将执行逻辑删除。
     * 如果没有配置逻辑删除，它将执行物理删除。
     *
     * @param userId 要删除的用户ID。
     * @return 被删除的用户实体。
     * @throws ResourceNotFoundException 如果用户不存在。
     */
    User deleteUser(Long userId);

    /**
     * 根据用户名加载用户详情，用于 Spring Security 认证。
     * 此方法也暴露在 UserService 接口中，方便其他服务调用以获取完整的 User 对象。
     *
     * @param username 用户名。
     * @return 用户详情 (User 实体，已实现 UserDetails 接口)。
     * @throws UsernameNotFoundException 如果用户不存在。
     */
    UserDetails loadUserByUsername(String username) throws UsernameNotFoundException;
}
