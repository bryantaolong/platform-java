package com.bryan.platform.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bryan.platform.common.exception.BusinessException;
import com.bryan.platform.common.exception.ResourceNotFoundException;
import com.bryan.platform.model.request.UserExportRequest;
import com.bryan.platform.model.request.UserUpdateRequest;
import com.bryan.platform.model.entity.User;
import com.bryan.platform.dao.mapper.UserMapper;
import com.bryan.platform.model.vo.UserExportVO;
import jakarta.servlet.http.HttpServletResponse;
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
    private final ExcelExportService excelExportService;

    private static final LinkedHashMap<String, String> EXPORT_FIELDS;

    static {
        EXPORT_FIELDS = new LinkedHashMap<>();
        EXPORT_FIELDS.put("id", "用户ID");
        EXPORT_FIELDS.put("username", "用户名");
        EXPORT_FIELDS.put("email", "邮箱");
        EXPORT_FIELDS.put("roles", "角色");
        EXPORT_FIELDS.put("statusText", "状态");
        EXPORT_FIELDS.put("createTime", "创建时间");
        EXPORT_FIELDS.put("updateTime", "更新时间");
    }

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
     */
    public User getUserById(Long userId) {
        // 1. 根据ID查询用户
        return userMapper.selectById(userId);
    }

    /**
     * 根据用户名获取用户信息。
     *
     * @param username 用户名
     * @return 用户实体对象
     */
    public User getUserByUsername(String username) {
        // 1. 根据用户名查询用户
        return userMapper.selectByUsername(username);
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
                        User userWithSameUsername = userMapper.selectByUsername(userUpdateRequest.getUsername());
                        if (userWithSameUsername != null && !userWithSameUsername.getId().equals(userId)) {
                            throw new BusinessException("用户名已存在");
                        }
                        existingUser.setUsername(userUpdateRequest.getUsername());
                    }

                    // 2. 更新邮箱信息
                    if (userUpdateRequest.getEmail() != null) {
                        existingUser.setEmail(userUpdateRequest.getEmail());
                    }

                    // 3. 执行数据库更新
                    userMapper.updateById(existingUser);

                    // 4. 记录日志并返回
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

                    // 2. 更新数据库
                    userMapper.updateById(existingUser);

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
        return Optional.ofNullable(userMapper.selectById(userId))
                .map(existingUser -> {
                    // 1. 验证旧密码是否正确
                    if (!passwordEncoder.matches(oldPassword, existingUser.getPassword())) {
                        throw new BusinessException("旧密码不正确");
                    }

                    // 2. 设置新密码（加密）
                    existingUser.setPassword(passwordEncoder.encode(newPassword));

                    // 3. 更新数据库
                    userMapper.updateById(existingUser);

                    // 4. 记录日志
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

                    // 2. 更新数据库
                    userMapper.updateById(existingUser);

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
        return Optional.ofNullable(userMapper.selectById(userId))
                .map(existingUser -> {
                    // 1. 设置状态为正常
                    existingUser.setStatus(0);

                    // 2. 更新数据库
                    userMapper.updateById(existingUser);

                    // 3. 记录日志
                    log.info("用户ID: {} 解封成功", userId);
                    return existingUser;
                })
                .orElseThrow(() -> new ResourceNotFoundException("用户ID: " + userId + " 不存在"));
    }

    /**
     * 校验用户是否存在。
     *
     * @param userId 用户ID
     * @throws BusinessException 用户不存在时抛出
     */
    public void validateUserExists(Long userId) {
        // 1. 查询用户是否存在
        if (userMapper.selectById(userId) == null) {
            throw new BusinessException("用户不存在");
        }
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
                    // 1. 执行逻辑删除（依赖 @TableLogic）
                    userMapper.deleteById(userId);

                    // 2. 记录日志
                    log.info("用户ID: {} 删除成功 (逻辑删除)", userId);
                    return existingUser;
                })
                .orElseThrow(() -> new ResourceNotFoundException("用户ID: " + userId + " 不存在"));
    }

    /**
     * 导出用户数据（支持动态字段选择）。
     *
     * @param response HTTP 响应对象
     * @param request  导出请求参数（包含状态和字段）
     */
    public void exportUsers(HttpServletResponse response, UserExportRequest request) {
        // 1. 查询符合条件的用户数据
        List<Map<String, Object>> userData = userMapper.selectUsersForExport(request.getStatus());

        // 2. 转换状态字段为文字描述
        for (Map<String, Object> row : userData) {
            Integer status = (Integer) row.get("status");
            row.put("statusText", status != null && status == 0 ? "正常" : "封禁");
        }

        // 3. 过滤导出字段
        LinkedHashMap<String, String> selectedFields = new LinkedHashMap<>();
        if (request.getFields() == null || request.getFields().isEmpty()) {
            selectedFields.putAll(EXPORT_FIELDS);
        } else {
            for (String key : EXPORT_FIELDS.keySet()) {
                if (request.getFields().contains(key)) {
                    selectedFields.put(key, EXPORT_FIELDS.get(key));
                }
            }
        }

        // 4. 构建导出数据
        List<Map<String, Object>> filteredData = new ArrayList<>();
        for (Map<String, Object> row : userData) {
            Map<String, Object> filteredRow = new LinkedHashMap<>();
            for (String key : selectedFields.keySet()) {
                filteredRow.put(key, row.get(key));
            }
            filteredData.add(filteredRow);
        }

        // 5. 执行导出
        String fileName = (request.getFileName() == null || request.getFileName().isEmpty()) ? "用户数据" : request.getFileName();
        excelExportService.exportDynamicExcel(response, filteredData, selectedFields, fileName);
        log.info("导出用户数据完成，导出条数：{}", filteredData.size());
    }

    /**
     * 导出所有用户数据（字段固定，使用实体类）。
     *
     * @param response HTTP 响应对象
     * @param status   用户状态过滤条件
     * @param fileName 导出文件名
     */
    public void exportAllUsers(HttpServletResponse response, Integer status, String fileName) {
        // 1. 查询数据并封装为 VO
        List<UserExportVO> users = userMapper.selectUsersForExport(status).stream().map(map -> {
            UserExportVO vo = new UserExportVO();
            vo.setId(Long.valueOf(map.get("id").toString()));
            vo.setUsername((String) map.get("username"));
            vo.setEmail((String) map.get("email"));
            vo.setRoles((String) map.get("roles"));

            Integer s = (Integer) map.get("status");
            vo.setStatus(s);
            vo.setStatusText(s != null && s == 0 ? "正常" : "封禁");

            vo.setCreateTime((String) map.get("createTime"));
            vo.setUpdateTime((String) map.get("updateTime"));
            return vo;
        }).toList();

        // 2. 设置导出文件名
        String finalFileName = (fileName == null || fileName.isEmpty()) ? "用户数据" : fileName;

        // 3. 执行导出
        excelExportService.exportToExcel(response, users, UserExportVO.class, finalFileName);
        log.info("导出所有用户数据完成，条数：{}", users.size());
    }
}
