package com.bryan.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bryan.platform.common.exception.BusinessException;
import com.bryan.platform.common.exception.ResourceNotFoundException;
import com.bryan.platform.common.util.JwtUtil;
import com.bryan.platform.model.dto.UserUpdateDTO;
import com.bryan.platform.model.entity.User;
import com.bryan.platform.model.dto.UserLoginDTO;
import com.bryan.platform.model.dto.UserRegisterDTO;
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
public class UserServiceImpl implements UserService, UserDetailsService {

    private final UserMapper userMapper;

    private final PasswordEncoder passwordEncoder;

    /**
     * 用户注册逻辑。
     *
     * @param userRegisterDTO 用户注册数据传输对象
     * @return 注册成功的用户实体
     * @throws RuntimeException 如果用户名已存在
     */
    @Override
    public User register(UserRegisterDTO userRegisterDTO) {
        // 检查用户名是否已存在
        if (userMapper.selectByUsername(userRegisterDTO.getUsername()) != null) {
            throw new RuntimeException("用户名已存在");
        }
        // 构建用户实体，密码进行加密，默认状态为0，默认角色为 ROLE_USER
        User user = User.builder()
                .username(userRegisterDTO.getUsername())
                .password(passwordEncoder.encode(userRegisterDTO.getPassword()))
                .email(userRegisterDTO.getEmail())
                .status(0)
                .roles("ROLE_USER") // 注册时默认赋予 ROLE_USER 角色
                .build();
        userMapper.insert(user);
        return user;
    }

    /**
     * 用户登录逻辑。
     *
     * @param userLoginDTO 用户登录数据传输对象
     * @return JWT Token 字符串
     * @throws RuntimeException 如果用户名或密码错误
     */
    @Override
    public String login(UserLoginDTO userLoginDTO) {
        User user = userMapper.selectByUsername(userLoginDTO.getUsername());
        // 验证用户名和密码
        if (user == null || !passwordEncoder.matches(userLoginDTO.getPassword(), user.getPassword())) {
            throw new RuntimeException("用户名或密码错误");
        }

        // --- 关键修改：在生成 JWT Token 时包含用户角色信息 ---
        Map<String, Object> claims = new HashMap<>();
        // 将用户的角色字符串添加到 JWT Claims 中
        // 注意：这里直接将 roles 字符串放入，JwtUtil会将其转换为List<String>
        claims.put("roles", user.getRoles());
        // 可以根据需要添加其他信息，例如 email
        // claims.put("email", user.getEmail());

        // 使用 JwtUtil 生成包含 userId 和 roles 的 Token
        return JwtUtil.generateToken(user.getId().toString(), claims);
    }

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
     * @param userUpdateDTO 包含需要更新的用户信息（用户名、邮箱）。密码和角色修改请使用专门方法。
     * @return 更新后的用户实体。
     * @throws ResourceNotFoundException 如果用户不存在。
     * @throws BusinessException         如果尝试更新的用户名已存在（且不是当前用户自身）。
     */
    @Override
    public User updateUser(Long userId, UserUpdateDTO userUpdateDTO) {
        return Optional.ofNullable(userMapper.selectById(userId))
                .map(existingUser -> {
                    // 检查用户名是否重复且不是当前用户自身
                    if (userUpdateDTO.getUsername() != null && !userUpdateDTO.getUsername().equals(existingUser.getUsername())) {
                        User userWithSameUsername = userMapper.selectByUsername(userUpdateDTO.getUsername());
                        if (userWithSameUsername != null && !userWithSameUsername.getId().equals(userId)) {
                            throw new BusinessException("用户名已存在");
                        }
                        existingUser.setUsername(userUpdateDTO.getUsername());
                    }
                    if (userUpdateDTO.getEmail() != null) {
                        existingUser.setEmail(userUpdateDTO.getEmail());
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

    /**
     * 获取当前登录用户实体。
     *
     * @return 当前登录用户实体
     */
    @Override
    public User getCurrentUser() {
        Long userId = JwtUtil.getCurrentUserId();
        return userMapper.selectById(userId);
    }

    /**
     * 实现 UserDetailsService 接口方法，用于 Spring Security 加载用户详情。
     *
     * @param username 用户名
     * @return 用户详情 (UserDetails 实例)
     * @throws UsernameNotFoundException 如果用户不存在
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userMapper.selectByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("用户不存在: " + username);
        }
        return user; // user 实体已经实现了 UserDetails 接口，并正确解析了 roles
    }
}
