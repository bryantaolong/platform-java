package com.bryan.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bryan.platform.common.exception.BusinessException;
import com.bryan.platform.common.exception.ResourceNotFoundException;
import com.bryan.platform.common.util.JwtUtil;
import com.bryan.platform.model.request.LoginRequest;
import com.bryan.platform.model.request.UserUpdateRequest;
import com.bryan.platform.model.entity.User;
import com.bryan.platform.model.request.RegisterRequest;
import com.bryan.platform.dao.mapper.UserMapper;
import com.bryan.platform.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.security.core.userdetails.UserDetails; // Add this import
import org.springframework.security.core.userdetails.UserDetailsService; // Add this import
import org.springframework.security.core.userdetails.UsernameNotFoundException; // Add this import

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;


/**
 * ClassName: UserServiceImpl
 * Package: com.bryan.platform.service.impl
 * Description: 用户服务实现类，处理用户注册、登录、信息获取等业务。
 * Author: Bryan Long
 * Create: 2025/6/19 - 19:50
 * Version: v1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;

    private final PasswordEncoder passwordEncoder;

    /**
     * 获取所有用户列表（支持分页，但此处由于接口未提供 Pageable，默认返回所有用户）。
     *
     * @return 包含所有用户数据的 Page 对象。
     */
    @Override
    public Page<User> getAllUsers() {
        // 创建一个 Page 对象，表示获取所有数据，currentPage=1, pageSize=Integer.MAX_VALUE
        // 如果需要真正的分页，UserService 接口的 getAllUsers 方法应接受 Pageable 参数
        Page<User> page = new Page<>(1, Integer.MAX_VALUE);
        return userMapper.selectPage(page, new QueryWrapper<>()); // 查询所有用户
    }

    /**
     * 根据用户ID获取用户实体。
     *
     * @param userId 用户的数据库主键 ID。
     * @return 对应的用户实体，如果不存在则返回 null。
     */
    @Override
    public User getUserById(Long userId) {
        return userMapper.selectById(userId);
    }

    /**
     * 根据用户名获取用户实体。
     * 此方法现在是 UserService 接口的显式实现。
     *
     * @param username 用户名。
     * @return 对应的用户实体，如果不存在则返回 null。
     */
    @Override
    public User getUserByUsername(String username) {
        return userMapper.selectByUsername(username);
    }

    /**
     * 更新用户基本信息。
     *
     * @param userId  要更新的用户ID。
     * @param userUpdateRequest 包含需要更新的用户信息（用户名、邮箱）。密码和角色修改请使用专门方法。
     * @return 更新后的用户实体。
     * @throws ResourceNotFoundException 如果用户不存在。
     * @throws BusinessException         如果尝试更新的用户名已存在（且不是当前用户自身）。
     */
    @Override
    public User updateUser(Long userId, UserUpdateRequest userUpdateRequest) {
        return Optional.ofNullable(userMapper.selectById(userId))
                .map(existingUser -> {
                    // 检查用户名是否重复且不是当前用户自身
                    if (userUpdateRequest.getUsername() != null && !userUpdateRequest.getUsername().equals(existingUser.getUsername())) {
                        User userWithSameUsername = userMapper.selectByUsername(userUpdateRequest.getUsername());
                        if (userWithSameUsername != null && !userWithSameUsername.getId().equals(userId)) {
                            throw new BusinessException("用户名已存在");
                        }
                        existingUser.setUsername(userUpdateRequest.getUsername());
                    }
                    if (userUpdateRequest.getEmail() != null) {
                        existingUser.setEmail(userUpdateRequest.getEmail());
                    }
                    userMapper.updateById(existingUser);
                    log.info("用户ID: {} 的信息更新成功", userId);
                    return existingUser;
                })
                .orElseThrow(() -> new ResourceNotFoundException("用户ID: " + userId + " 不存在"));
    }

    /**
     * 更改用户角色。
     * 此操作通常需要管理员权限。
     *
     * @param userId 要更改角色的用户ID。
     * @param roles  新的角色字符串，多个角色以逗号分隔（例如 "ROLE_USER,ROLE_ADMIN"）。
     * @return 更新后的用户实体。
     * @throws ResourceNotFoundException 如果用户不存在。
     */
    @Override
    public User changeRole(Long userId, String roles) {
        return Optional.ofNullable(userMapper.selectById(userId))
                .map(existingUser -> {
                    existingUser.setRoles(roles);
                    userMapper.updateById(existingUser);
                    log.info("用户ID: {} 的角色更新成功为: {}", userId, roles);
                    return existingUser;
                })
                .orElseThrow(() -> new ResourceNotFoundException("用户ID: " + userId + " 不存在"));
    }

    /**
     * 更改用户密码。
     *
     * @param userId      要更改密码的用户ID。
     * @param oldPassword 旧密码（明文）。
     * @param newPassword 新密码（明文）。
     * @return 更新后的用户实体。
     * @throws ResourceNotFoundException 如果用户不存在。
     * @throws BusinessException         如果旧密码不正确。
     */
    @Override
    public User changePassword(Long userId, String oldPassword, String newPassword) {
        return Optional.ofNullable(userMapper.selectById(userId))
                .map(existingUser -> {
                    // 验证旧密码是否正确
                    if (!passwordEncoder.matches(oldPassword, existingUser.getPassword())) {
                        throw new BusinessException("旧密码不正确");
                    }
                    // 加密新密码并设置
                    existingUser.setPassword(passwordEncoder.encode(newPassword));
                    userMapper.updateById(existingUser);
                    log.info("用户ID: {} 的密码更新成功", userId);
                    return existingUser;
                })
                .orElseThrow(() -> new ResourceNotFoundException("用户ID: " + userId + " 不存在"));
    }

    /**
     * 封禁用户。
     * 此操作通常需要管理员权限。
     *
     * @param userId 要更改角色的用户ID。
     * @return 更新后的用户实体。
     * @throws ResourceNotFoundException 如果用户不存在。
     */
    @Override
    public User blockUser(Long userId) {
        return Optional.ofNullable(userMapper.selectById(userId))
                .map(existingUser -> {
                    existingUser.setStatus(1);
                    userMapper.updateById(existingUser);
                    log.info("用户ID: {} 封禁成功", userId);
                    return existingUser;
                })
                .orElseThrow(() -> new ResourceNotFoundException("用户ID: " + userId + " 不存在"));
    }

    /**
     * 解封用户。
     * 此操作通常需要管理员权限。
     *
     * @param userId 要更改角色的用户ID。
     * @return 更新后的用户实体。
     * @throws ResourceNotFoundException 如果用户不存在。
     */
    @Override
    public User unblockUser(Long userId) {
        return Optional.ofNullable(userMapper.selectById(userId))
                .map(existingUser -> {
                    existingUser.setStatus(1);
                    userMapper.updateById(existingUser);
                    log.info("用户ID: {} 解封成功", userId);
                    return existingUser;
                })
                .orElseThrow(() -> new ResourceNotFoundException("用户ID: " + userId + " 不存在"));
    }

    /**
     * 删除用户（逻辑删除）。
     * 假设 User 实体配置了 @TableLogic 注解，此方法将执行逻辑删除。
     * 如果没有配置逻辑删除，它将执行物理删除。
     *
     * @param userId 要删除的用户ID。
     * @return 被删除的用户实体。
     * @throws ResourceNotFoundException 如果用户不存在。
     */
    @Override
    public User deleteUser(Long userId) {
        return Optional.ofNullable(userMapper.selectById(userId))
                .map(existingUser -> {
                    // Mybatis-Plus 的 deleteById 会根据 @TableLogic 注解执行逻辑删除
                    userMapper.deleteById(userId);
                    log.info("用户ID: {} 删除成功 (逻辑删除)", userId);
                    return existingUser;
                })
                .orElseThrow(() -> new ResourceNotFoundException("用户ID: " + userId + " 不存在"));
    }
}
