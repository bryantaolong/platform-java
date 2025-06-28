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
public class UserService {

    private final UserMapper userMapper;

    private final PasswordEncoder passwordEncoder;

    private final ExcelExportService excelExportService;

    /**
     * 获取所有用户列表（支持分页，但此处由于接口未提供 Pageable，默认返回所有用户）。
     *
     * @return 包含所有用户数据的 Page 对象。
     */
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
     * 支持动态字段选择的用户数据导出
     */
    public void exportUsers(HttpServletResponse response, UserExportRequest request) {
        List<Map<String, Object>> userData = userMapper.selectUsersForExport(request.getStatus());

        // 状态字段转换成文字显示
        for (Map<String, Object> row : userData) {
            Integer status = (Integer) row.get("status");
            row.put("statusText", status != null && status == 0 ? "正常" : "封禁");
        }

        // 根据请求字段过滤（默认导出全部）
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

        // 过滤数据字段，只保留选中的字段
        List<Map<String, Object>> filteredData = new ArrayList<>();
        for (Map<String, Object> row : userData) {
            Map<String, Object> filteredRow = new LinkedHashMap<>();
            for (String key : selectedFields.keySet()) {
                filteredRow.put(key, row.get(key));
            }
            filteredData.add(filteredRow);
        }

        // 调用通用ExcelService导出
        String fileName = (request.getFileName() == null || request.getFileName().isEmpty()) ? "用户数据" : request.getFileName();
        excelExportService.exportDynamicExcel(response, filteredData, selectedFields, fileName);
        log.info("导出用户数据完成，导出条数：{}", filteredData.size());
    }

    /**
     * 导出所有用户数据，包含所有字段（实体类方式）
     */
    public void exportAllUsers(HttpServletResponse response, Integer status, String fileName) {
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

        String finalFileName = (fileName == null || fileName.isEmpty()) ? "用户数据" : fileName;
        excelExportService.exportToExcel(response, users, UserExportVO.class, finalFileName);
        log.info("导出所有用户数据完成，条数：{}", users.size());
    }
}
