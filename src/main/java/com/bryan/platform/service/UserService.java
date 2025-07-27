package com.bryan.platform.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bryan.platform.common.exception.BusinessException;
import com.bryan.platform.common.exception.ResourceNotFoundException;
import com.bryan.platform.model.request.PageRequest;
import com.bryan.platform.model.request.UserSearchRequest;
import com.bryan.platform.model.request.UserUpdateRequest;
import com.bryan.platform.model.entity.User;
import com.bryan.platform.dao.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 用户服务实现类，处理用户注册、登录、信息管理、导出等业务逻辑。
 *
 * @author Bryan
 * @since 2025/6/19
 * @version 2.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final AuthService authService;

    /**
     * 获取所有用户列表（不分页）。
     *
     * @return 包含所有用户的分页对象（Page）。
     */
    public Page<User> getAllUsers() {
        // 1. 构造查询条件，默认获取全部数据
        Page<User> page = new Page<>(1, Integer.MAX_VALUE);

        // 2. 执行查询并返回结果
        return userMapper.selectPage(page, new QueryWrapper<>());
    }

    /**
     * 根据用户ID获取用户信息。
     *
     * @param userId 用户ID
     * @return 用户实体对象
     * @throws ResourceNotFoundException 用户不存在时抛出
     */
    public User getUserById(Long userId) {
        // 1. 根据ID查询用户
        User user = userMapper.selectById(userId);

        if (user == null) {
            throw new ResourceNotFoundException("用户不存在");
        }

        return user;
    }

    /**
     * 根据用户名获取用户信息。
     *
     * @param username 用户名
     * @return 用户实体对象
     */
    public User getUserByUsername(String username) {
        // 1. 根据用户名查询用户
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username);

        return userMapper.selectOne(queryWrapper);
    }

    /**
     * 通用用户搜索，支持多条件模糊查询和分页。
     *
     * @param searchRequest 搜索请求
     * @param pageRequest 分页请求
     * @return 符合查询条件的分页对象（Page）
     */
    public Page<User> searchUsers(UserSearchRequest searchRequest, PageRequest pageRequest) {
        // 1. 构造查询条件
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();

        // 2. 添加用户名模糊查询
        if (searchRequest.getUsername() != null && !searchRequest.getUsername().trim().isEmpty()) {
            queryWrapper.like("username", searchRequest.getUsername());
        }
        // 3. 添加手机号模糊查询
        if (searchRequest.getPhoneNumber() != null && !searchRequest.getPhoneNumber().trim().isEmpty()) {
            queryWrapper.like("phone_number", searchRequest.getPhoneNumber());
        }
        // 4. 添加邮箱模糊查询
        if (searchRequest.getEmail() != null && !searchRequest.getEmail().trim().isEmpty()) {
            queryWrapper.like("email", searchRequest.getEmail());
        }
        // 5. 添加性别精确查询
        if (searchRequest.getGender() != null) {
            queryWrapper.eq("gender", searchRequest.getGender());
        }
        // 6. 添加状态精确查询
        if (searchRequest.getStatus() != null) {
            queryWrapper.eq("status", searchRequest.getStatus());
        }

        // 7. 构造分页对象
        Page<User> page = new Page<>(pageRequest.getPageNum(), pageRequest.getPageSize());

        // 8. 执行查询并返回结果
        return userMapper.selectPage(page, queryWrapper);
    }

    /**
     * 更新用户基础信息（用户名和邮箱）。
     *
     * @param userId            用户ID
     * @param userUpdateRequest 用户更新请求体
     * @return 更新后的用户对象
     * @throws ResourceNotFoundException 用户不存在时抛出
     * @throws BusinessException         用户名重复时抛出
     */
    public User updateUser(Long userId, UserUpdateRequest userUpdateRequest) {
        return Optional.ofNullable(userMapper.selectById(userId))
                .map(existingUser -> {
                    // 1. 检查用户名是否重复
                    if (userUpdateRequest.getUsername() != null &&
                            !userUpdateRequest.getUsername().equals(existingUser.getUsername())) {
                        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
                        queryWrapper.eq("username", existingUser.getUsername());

                        User userWithSameUsername = userMapper.selectOne(queryWrapper);
                        if (userWithSameUsername != null && !userWithSameUsername.getId().equals(userId)) {
                            throw new BusinessException("用户名已存在");
                        }
                        existingUser.setUsername(userUpdateRequest.getUsername());
                    }

                    // 2. 更新电话号码
                    if (userUpdateRequest.getPhoneNumber() != null) {
                        existingUser.setPhoneNumber(userUpdateRequest.getPhoneNumber());
                    }

                    // 3. 更新邮箱信息
                    if (userUpdateRequest.getEmail() != null) {
                        existingUser.setEmail(userUpdateRequest.getEmail());
                    }

                    // 4. 更新性别
                    if (userUpdateRequest.getGender() != null) {
                        existingUser.setGender(userUpdateRequest.getGender());
                    }

                    // 5. 更新头像
                    if (userUpdateRequest.getAvatar() != null) {
                        existingUser.setAvatar(userUpdateRequest.getAvatar());
                    }

                    // 6. 更新操作员信息
                    String operator = authService.getCurrentUsername();
                    existingUser.setUpdateBy(operator);

                    // 7. 执行数据库更新
                    userMapper.updateById(existingUser);

                    // 8. 记录日志并返回
                    log.info("用户ID: {} 的信息更新成功", userId);
                    return existingUser;
                })
                .orElseThrow(() -> new ResourceNotFoundException("用户ID: " + userId + " 不存在"));
    }

    /**
     * 修改用户角色。
     *
     * @param userId 用户ID
     * @param roles  新角色字符串（多个角色用逗号分隔）
     * @return 更新后的用户对象
     * @throws ResourceNotFoundException 用户不存在时抛出
     */
    public User changeRole(Long userId, String roles) {
        return Optional.ofNullable(userMapper.selectById(userId))
                .map(existingUser -> {
                    // 1. 设置角色字段
                    existingUser.setRoles(roles);

                    // 2. 更新操作员信息
                    String operator = authService.getCurrentUsername();
                    existingUser.setUpdateBy(operator);

                    // 3. 更新数据库
                    userMapper.updateById(existingUser);

                    // 4. 记录日志
                    log.info("用户ID: {} 的角色更新成功为: {}", userId, roles);
                    return existingUser;
                })
                .orElseThrow(() -> new ResourceNotFoundException("用户ID: " + userId + " 不存在"));
    }

    /**
     * 修改用户密码。
     *
     * @param userId      用户ID
     * @param oldPassword 旧密码（明文）
     * @param newPassword 新密码（明文）
     * @return 更新后的用户对象
     * @throws ResourceNotFoundException 用户不存在时抛出
     * @throws BusinessException         旧密码验证失败时抛出
     */
    public User changePassword(Long userId, String oldPassword, String newPassword) {
        return Optional.ofNullable(userMapper.selectById(userId))
                .map(existingUser -> {
                    // 1. 验证旧密码是否正确
                    if (!passwordEncoder.matches(oldPassword, existingUser.getPassword())) {
                        throw new BusinessException("旧密码不正确");
                    }

                    // 2. 设置新密码（加密）
                    existingUser.setPassword(passwordEncoder.encode(newPassword));

                    // 3. 更新操作员信息
                    String operator = authService.getCurrentUsername();
                    existingUser.setUpdateBy(operator);

                    // 4. 更新数据库
                    userMapper.updateById(existingUser);

                    // 5. 记录日志
                    log.info("用户ID: {} 的密码更新成功", userId);
                    return existingUser;
                })
                .orElseThrow(() -> new ResourceNotFoundException("用户ID: " + userId + " 不存在"));
    }

    /**
     * 封禁指定用户。
     *
     * @param userId 用户ID
     * @return 更新后的用户对象
     * @throws ResourceNotFoundException 用户不存在时抛出
     */
    public User blockUser(Long userId) {
        return Optional.ofNullable(userMapper.selectById(userId))
                .map(existingUser -> {
                    // 1. 设置状态为封禁
                    existingUser.setStatus(1);

                    // 2. 更新操作员信息
                    String operator = authService.getCurrentUsername();
                    existingUser.setUpdateBy(operator);

                    // 3. 更新数据库
                    userMapper.updateById(existingUser);

                    // 4. 记录日志
                    log.info("用户ID: {} 封禁成功", userId);
                    return existingUser;
                })
                .orElseThrow(() -> new ResourceNotFoundException("用户ID: " + userId + " 不存在"));
    }

    /**
     * 解封指定用户。
     *
     * @param userId 用户ID
     * @return 更新后的用户对象
     * @throws ResourceNotFoundException 用户不存在时抛出
     */
    public User unblockUser(Long userId) {
        return Optional.ofNullable(userMapper.selectById(userId))
                .map(existingUser -> {
                    // 1. 设置状态为正常
                    existingUser.setStatus(0);

                    // 2. 更新操作员信息
                    String operator = authService.getCurrentUsername();
                    existingUser.setUpdateBy(operator);

                    // 3. 更新数据库
                    userMapper.updateById(existingUser);

                    // 4. 记录日志
                    log.info("用户ID: {} 解封成功", userId);
                    return existingUser;
                })
                .orElseThrow(() -> new ResourceNotFoundException("用户ID: " + userId + " 不存在"));
    }

    /**
     * 删除用户（逻辑删除）。
     *
     * @param userId 用户ID
     * @return 被删除的用户对象
     * @throws ResourceNotFoundException 用户不存在时抛出
     */
    public User deleteUser(Long userId) {
        return Optional.ofNullable(userMapper.selectById(userId))
                .map(existingUser -> {
                    // 1. 更新操作员信息
                    String operator = authService.getCurrentUsername();
                    existingUser.setUpdateBy(operator);

                    // 2. 更新数据库
                    userMapper.updateById(existingUser);

                    // 3. 执行逻辑删除（依赖 @TableLogic）
                    userMapper.deleteById(userId);

                    // 4. 记录日志
                    log.info("用户ID: {} 删除成功 (逻辑删除)", userId);
                    return existingUser;
                })
                .orElseThrow(() -> new ResourceNotFoundException("用户ID: " + userId + " 不存在"));
    }
}
