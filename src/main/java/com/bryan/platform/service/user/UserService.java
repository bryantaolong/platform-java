package com.bryan.platform.service.user;

import com.bryan.platform.domain.entity.user.SysUser;
import com.bryan.platform.domain.entity.user.UserRole;
import com.bryan.platform.domain.enums.UserStatusEnum;
import com.bryan.platform.domain.request.user.ChangeRoleRequest;
import com.bryan.platform.domain.request.user.UserSearchRequest;
import com.bryan.platform.domain.request.user.UserUpdateRequest;
import com.bryan.platform.domain.response.PageResult;
import com.bryan.platform.exception.BusinessException;
import com.bryan.platform.exception.ResourceNotFoundException;
import com.bryan.platform.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 用户服务实现类，处理用户注册、登录、信息管理、导出等业务逻辑。
 *
 * @author Bryan Long
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final UserRoleService userRoleService;

    private PageResult<SysUser> page(long pageNum,
                                     long pageSize,
                                     List<SysUser> rows,
                                     long total) {
        return PageResult.<SysUser>builder()
                .rows(rows)
                .total(total)
                .pageNum(pageNum)
                .pageSize(pageSize)
                .build();
    }

    /**
     * 获取所有用户列表（不分页）。
     *
     * @return 包含所有用户的分页对象（Page）。
     */
    public PageResult<SysUser> getAllUsers(int pageNum,
                                           int pageSize) {
        int offset = (pageNum - 1) * pageSize;
        List<SysUser> rows = userMapper.selectPage(
                offset,
                pageSize,
                null,
                null);
        long total = userMapper.count(null, null);
        return page(pageNum, pageSize, rows, total);
    }

    /**
     * 根据用户ID获取用户信息。
     *
     * @param userId 用户ID
     * @return 用户实体对象
     * @throws ResourceNotFoundException 用户不存在时抛出
     */
    public SysUser getUserById(Long userId) {
        return Optional.ofNullable(userMapper.selectById(userId))
                .orElseThrow(() -> new ResourceNotFoundException("用户不存在"));
    }

    /**
     * 根据用户名获取用户信息。
     *
     * @param username 用户名
     * @return 用户实体对象
     */
    public SysUser getUserByUsername(String username) {
        return userMapper.selectByUsername(username);
    }

    /**
     * 通用用户搜索，支持多条件模糊查询和分页。
     *
     * @param searchRequest 搜索请求
     * @return 符合查询条件的分页对象（Page）
     */
    public PageResult<SysUser> searchUsers(UserSearchRequest searchRequest,
                                           int pageNum,
                                           int pageSize) {
        int offset = (pageNum - 1) * pageSize;
        List<SysUser> rows = userMapper.selectPage(
                offset,
                pageSize,
                searchRequest,
                null);
        long total = userMapper.count(searchRequest, null);
        return page(pageNum, pageSize, rows, total);
    }

    public SysUser save(SysUser sysUser) {
        if (sysUser.getId() == null) {
            userMapper.insert(sysUser);
        } else {
            userMapper.update(sysUser);
        }
        return sysUser;
    }

    /**
     * 更新用户基础信息（用户名和邮箱）。
     *
     * @param userId            用户ID
     * @param req 用户更新请求体
     * @return 更新后的用户对象
     * @throws ResourceNotFoundException 用户不存在时抛出
     * @throws BusinessException         用户名重复时抛出
     */
    public SysUser updateUser(Long userId, UserUpdateRequest req) {
        SysUser user = getUserById(userId);

        // 用户名不能重复
        if (req.getUsername() != null &&
                !req.getUsername().equals(user.getUsername())) {
            SysUser sameName = userMapper.selectByUsername(req.getUsername());
            if (sameName != null && !sameName.getId().equals(userId)) {
                throw new BusinessException("用户名已存在");
            }
            user.setUsername(req.getUsername());
        }
        if (req.getPhone() != null) user.setPhone(req.getPhone());
        if (req.getEmail() != null) user.setEmail(req.getEmail());

        userMapper.update(user);
        log.info("用户ID: {} 的信息更新成功", userId);
        return user;
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
    public SysUser changeRoleByIds(Long userId, ChangeRoleRequest req) {
        List<Long> ids = req.getRoleIds();
        List<UserRole> roles = userRoleService.findByIds(ids);

        if (roles.size() != ids.size()) {
            Set<Long> exist = roles.stream()
                    .map(UserRole::getId)
                    .collect(Collectors.toSet());
            ids.removeAll(exist);
            throw new IllegalArgumentException("角色不存在：" + ids);
        }
        String roleNames = roles.stream()
                .map(UserRole::getRoleName)
                .collect(Collectors.joining(","));
        SysUser user = getUserById(userId);
        user.setRoles(roleNames);
        userMapper.update(user);
        return user;
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
    public SysUser changePassword(Long userId,
                                  String oldPassword,
                                  String newPassword) {
        SysUser user = getUserById(userId);
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BusinessException("旧密码不正确");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setPasswordResetAt(LocalDateTime.now());
        userMapper.update(user);
        log.info("用户ID: {} 的密码更新成功", userId);
        return user;
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
    public SysUser changePasswordForcefully(Long userId, String newPassword) {
        SysUser user = getUserById(userId);
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setPasswordResetAt(LocalDateTime.now());
        userMapper.update(user);
        log.info("用户ID: {} 的密码强制修改成功", userId);
        return user;
    }

    /**
     * 封禁指定用户。
     *
     * @param userId 用户ID
     * @return 更新后的用户对象
     * @throws ResourceNotFoundException 用户不存在时抛出
     */
    public SysUser blockUser(Long userId) {
        SysUser user = getUserById(userId);
        user.setStatus(UserStatusEnum.BANNED);
        userMapper.update(user);
        log.info("用户ID: {} 封禁成功", userId);
        return user;
    }

    /**
     * 解封指定用户。
     *
     * @param userId 用户ID
     * @return 更新后的用户对象
     * @throws ResourceNotFoundException 用户不存在时抛出
     */
    public SysUser unblockUser(Long userId) {
        SysUser user = getUserById(userId);
        user.setStatus(UserStatusEnum.NORMAL);
        userMapper.update(user);
        log.info("用户ID: {} 解封成功", userId);
        return user;
    }

    /**
     * 删除用户（逻辑删除）。
     *
     * @param userId 用户ID
     * @return 被删除的用户对象
     * @throws ResourceNotFoundException 用户不存在时抛出
     */
    public SysUser deleteUser(Long userId) {
        SysUser user = getUserById(userId);
        userMapper.updateDeletedById(userId, 1);
        log.info("用户ID: {} 删除成功 (逻辑删除)", userId);
        return user;
    }
}
