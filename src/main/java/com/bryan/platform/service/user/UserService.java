package com.bryan.platform.service.user;

import com.bryan.platform.domain.entity.user.UserRole;
import com.bryan.platform.domain.enums.UserStatusEnum;
import com.bryan.platform.domain.request.ChangeRoleRequest;
import com.bryan.platform.exception.BusinessException;
import com.bryan.platform.exception.ResourceNotFoundException;
import com.bryan.platform.domain.request.user.UserSearchRequest;
import com.bryan.platform.domain.request.user.UserUpdateRequest;
import com.bryan.platform.domain.entity.user.User;
import com.bryan.platform.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

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
    private final UserRoleService userRoleService;

    /* ---------- 查询 ---------- */

    /**
     * 获取所有用户列表（不分页）。
     *
     * @return 包含所有用户的分页对象（Page）。
     */
    public org.springframework.data.domain.Page<User> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    /**
     * 根据用户ID获取用户信息。
     *
     * @param id 用户ID
     * @return 用户实体对象
     * @throws ResourceNotFoundException 用户不存在时抛出
     */
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("用户不存在"));
    }

    /**
     * 根据用户名获取用户信息。
     *
     * @param username 用户名
     * @return 用户实体对象
     */
    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username).orElse(null);
    }

    /**
     * 通用用户搜索，支持多条件模糊查询和分页。
     *
     * @param req 搜索请求
     * @param pageable 分页请求
     * @return 符合查询条件的分页对象（Page）
     */
    public Page<User> searchUsers(UserSearchRequest req, Pageable pageable) {
        return userRepository.searchUsers(req, pageable);
    }

    /* ---------- 更新 ---------- */
    /**
     * 更新用户基础信息（用户名和邮箱）。
     *
     * @param id            用户ID
     * @param req 用户更新请求体
     * @return 更新后的用户对象
     * @throws ResourceNotFoundException 用户不存在时抛出
     * @throws BusinessException         用户名重复时抛出
     */
    @Transactional
    public User updateUser(Long id, UserUpdateRequest req) {
        return userRepository.findById(id).map(u -> {
            // 检查用户名是否重复
            if (StringUtils.hasText(req.getUsername()) &&
                    !req.getUsername().equals(u.getUsername()) &&
                    userRepository.existsByUsername(req.getUsername())) {
                throw new BusinessException("用户名已存在");
            }
            // 更新用户名
            Optional.ofNullable(req.getUsername()).ifPresent(u::setUsername);

            // 更新电话号码
            Optional.ofNullable(req.getPhoneNumber()).ifPresent(u::setPhoneNumber);

            // 更新邮箱信息
            Optional.ofNullable(req.getEmail()).ifPresent(u::setEmail);

            return userRepository.save(u);
        }).orElseThrow(() -> new ResourceNotFoundException("用户不存在"));
    }

    /**
     * 修改用户角色。
     *
     * @param userId 用户ID
     * @param req  新角色字符串（多个角色用逗号分隔）
     * @return 更新后的用户对象
     * @throws ResourceNotFoundException 用户不存在时抛出
     */
    @Transactional
    public User changeRoleByIds(Long userId, ChangeRoleRequest req) {
        List<Long> ids = req.getRoleIds();
        List<UserRole> roles = userRoleService.findByIds(ids);

        // 校验 id 是否全部存在
        if (roles.size() != ids.size()) {
            Set<Long> exist = roles.stream().map(UserRole::getId).collect(Collectors.toSet());
            ids.removeAll(exist);
            throw new IllegalArgumentException("角色不存在：" + ids);
        }

        String roleNames = roles.stream()
                .map(UserRole::getRoleName)
                .collect(Collectors.joining(","));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("用户不存在"));
        user.setRoles(roleNames);
        return userRepository.save(user);
    }

    /**
     * 修改用户密码。
     *
     * @param id      用户ID
     * @param oldPwd 旧密码（明文）
     * @param newPwd 新密码（明文）
     * @return 更新后的用户对象
     * @throws ResourceNotFoundException 用户不存在时抛出
     * @throws BusinessException         旧密码验证失败时抛出
     */
    @Transactional
    public User changePassword(Long id, String oldPwd, String newPwd) {
        return userRepository.findById(id).map(u -> {
            // 验证旧密码是否正确
            if (!passwordEncoder.matches(oldPwd, u.getPassword())) {
                throw new BusinessException("旧密码不正确");
            }
            // 更新数据库
            int rows = userRepository.updatePassword(
                    id, passwordEncoder.encode(newPwd), LocalDateTime.now());
            if (rows == 0) throw new ResourceNotFoundException("用户不存在");
            return u;
        }).orElseThrow(() -> new ResourceNotFoundException("用户不存在"));
    }

    /**
     * 强制修改用户密码（管理员）。
     *
     * @param id      用户ID
     * @param newPwd 新密码（明文）
     * @return 更新后的用户对象
     * @throws ResourceNotFoundException 用户不存在时抛出
     */
    @Transactional
    public User changePasswordForcefully(Long id, String newPwd) {
        int rows = userRepository.updatePassword(
                id, passwordEncoder.encode(newPwd), LocalDateTime.now());
        if (rows == 0) throw new ResourceNotFoundException("用户不存在");
        return getUserById(id);
    }

    /* ---------- 状态 ---------- */

    /**
     * 封禁指定用户。
     *
     * @param id 用户ID
     * @return 更新后的用户对象
     * @throws ResourceNotFoundException 用户不存在时抛出
     */
    @Transactional
    public User blockUser(Long id) {
        int rows = userRepository.updateStatus(id, UserStatusEnum.BANNED);
        if (rows == 0) throw new ResourceNotFoundException("用户不存在");
        return getUserById(id);
    }

    /**
     * 解封指定用户。
     *
     * @param id 用户ID
     * @return 更新后的用户对象
     * @throws ResourceNotFoundException 用户不存在时抛出
     */
    @Transactional
    public User unblockUser(Long id) {
        int rows = userRepository.updateStatus(id, UserStatusEnum.NORMAL);
        if (rows == 0) throw new ResourceNotFoundException("用户不存在");
        return getUserById(id);
    }

    /* ---------- 逻辑删除 ---------- */

    /**
     * 删除用户（逻辑删除）。
     *
     * @param id 用户ID
     * @return 被删除的用户对象
     */
    @Transactional
    public User deleteUser(Long id) {
        User user = getUserById(id);
        userRepository.delete(user);   // 触发 @SQLDelete
        return user;
    }
}
