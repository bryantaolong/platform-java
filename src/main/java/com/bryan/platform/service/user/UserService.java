package com.bryan.platform.service.user;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bryan.platform.common.enums.UserStatusEnum;
import com.bryan.platform.common.exception.BusinessException;
import com.bryan.platform.common.exception.ResourceNotFoundException;
import com.bryan.platform.model.request.PageRequest;
import com.bryan.platform.model.request.user.UserSearchRequest;
import com.bryan.platform.model.request.user.UserUpdateRequest;
import com.bryan.platform.model.entity.user.User;
import com.bryan.platform.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
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

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * 获取所有用户列表（不分页）。
     *
     * @return 包含所有用户的分页对象（Page）。
     */
    public Page<User> getAllUsers(PageRequest pageRequest) {
        return userRepository.findAll(pageRequest.getPageNum(), pageRequest.getPageSize());
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
        User user = userRepository.findById(userId);

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
        return userRepository.findByUsername(username);
    }

    /**
     * 通用用户搜索，支持多条件模糊查询和分页。
     *
     * @param searchRequest 搜索请求
     * @param pageRequest 分页请求
     * @return 符合查询条件的分页对象（Page）
     */
    public Page<User> searchUsers(UserSearchRequest searchRequest, PageRequest pageRequest) {
        return userRepository.searchUsers(searchRequest, pageRequest);
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
        return Optional.ofNullable(userRepository.findById(userId))
                .map(existingUser -> {
                    // 1. 检查用户名是否重复
                    if (userUpdateRequest.getUsername() != null &&
                            !userUpdateRequest.getUsername().equals(existingUser.getUsername())) {
                        User userWithSameUsername = userRepository.findByUsername(userUpdateRequest.getUsername());
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

                    // 4. 执行数据库更新
                    userRepository.save(existingUser);

                    // 5. 记录日志并返回
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
        return Optional.ofNullable(userRepository.findById(userId))
                .map(existingUser -> {
                    // 1. 设置角色字段
                    existingUser.setRoles(roles);

                    // 2. 更新数据库
                    userRepository.save(existingUser);

                    // 3. 记录日志
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
        return Optional.ofNullable(userRepository.findById(userId))
                .map(existingUser -> {
                    // 1. 验证旧密码是否正确
                    if (!passwordEncoder.matches(oldPassword, existingUser.getPassword())) {
                        throw new BusinessException("旧密码不正确");
                    }

                    // 2. 设置新密码（加密）
                    existingUser.setPassword(passwordEncoder.encode(newPassword));

                    // 3. 设置重置密码时间
                    existingUser.setPasswordResetTime(LocalDateTime.now());

                    // 4. 更新数据库
                    userRepository.save(existingUser);

                    // 5. 记录日志
                    log.info("用户ID: {} 的密码更新成功", userId);
                    return existingUser;
                })
                .orElseThrow(() -> new ResourceNotFoundException("用户ID: " + userId + " 不存在"));
    }

    /**
     * 强制修改用户密码（管理员）。
     *
     * @param userId      用户ID
     * @param newPassword 新密码（明文）
     * @return 更新后的用户对象
     * @throws ResourceNotFoundException 用户不存在时抛出
     * @throws BusinessException         旧密码验证失败时抛出
     */
    public User changePasswordForcefully(Long userId, String newPassword) {
        return Optional.ofNullable(userRepository.findById(userId))
                .map(existingUser -> {
                    // 1. 设置新密码（加密）
                    existingUser.setPassword(passwordEncoder.encode(newPassword));

                    // 2. 设置重置密码时间
                    existingUser.setPasswordResetTime(LocalDateTime.now());

                    // 3. 更新数据库
                    userRepository.save(existingUser);

                    // 4. 记录日志
                    log.info("用户ID: {} 的密码强制修改成功", userId);
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
        return Optional.ofNullable(userRepository.findById(userId))
                .map(existingUser -> {
                    // 1. 设置状态为封禁
                    existingUser.setStatus(UserStatusEnum.BANNED);

                    // 2. 更新数据库
                    userRepository.save(existingUser);

                    // 3. 记录日志
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
        return Optional.ofNullable(userRepository.findById(userId))
                .map(existingUser -> {
                    // 1. 设置状态为正常
                    existingUser.setStatus(UserStatusEnum.NORMAL);

                    // 2. 更新数据库
                    userRepository.save(existingUser);

                    // 3. 记录日志
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
        return Optional.ofNullable(userRepository.findById(userId))
                .map(existingUser -> {
                    // 1. 更新数据库
                    userRepository.save(existingUser);

                    // 2. 执行逻辑删除
                    userRepository.save(existingUser);

                    // 3. 记录日志
                    log.info("用户ID: {} 删除成功 (逻辑删除)", userId);
                    return existingUser;
                })
                .orElseThrow(() -> new ResourceNotFoundException("用户ID: " + userId + " 不存在"));
    }
}
