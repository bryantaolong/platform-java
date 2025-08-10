package com.bryan.platform.repository;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bryan.platform.mapper.UserMapper;
import com.bryan.platform.domain.entity.user.User;
import com.bryan.platform.domain.request.PageRequest;
import com.bryan.platform.domain.request.user.UserExportRequest;
import com.bryan.platform.domain.request.user.UserSearchRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

/**
 * UserRepository
 *
 * @author Bryan Long
 */
@Repository
@RequiredArgsConstructor
public class UserRepository {

    private final UserMapper userMapper;

    public User save(User user) {
        userMapper.insert(user);
        return user;
    }

    public User findById(Long id) {
        return userMapper.selectById(id);
    }

    public User findByUsername(String username) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username);

        return userMapper.selectOne(queryWrapper);
    }

    public List<User> findAllByIdInBatch(List<Long> ids) {
        return userMapper.selectBatchIds(ids);
    }

    public Page<User> findAll(long pageNum, long pageSize) {
        // 1. 构造查询条件，默认获取全部数据
        Page<User> page = new Page<>(pageNum, pageSize, true);

        // 2. 执行查询并返回结果
        return  userMapper.selectPage(page, new QueryWrapper<>());
    }

    public Page<User> findAll(long pageNum, long pageSize, UserExportRequest request) {
        QueryWrapper<User> wrapper = this.buildExcelExportQueryWrapper(request);
        return userMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
    }

    public Page<User> searchUsers(UserSearchRequest searchRequest, PageRequest pageRequest) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();

        // 1. 字符串类型字段的模糊查询
        addLikeCondition(queryWrapper, "username", searchRequest.getUsername());
        addLikeCondition(queryWrapper, "phone", searchRequest.getPhone());
        addLikeCondition(queryWrapper, "email", searchRequest.getEmail());
        addLikeCondition(queryWrapper, "roles", searchRequest.getRoles());
        addLikeCondition(queryWrapper, "last_login_ip", searchRequest.getLastLoginIp());
        addLikeCondition(queryWrapper, "created_by", searchRequest.getCreatedBy());
        addLikeCondition(queryWrapper, "updated_by", searchRequest.getUpdatedBy());

        // 2. 精确匹配字段
        addEqCondition(queryWrapper, "status", searchRequest.getStatus());
        addEqCondition(queryWrapper, "last_login_at", searchRequest.getLastLoginAt());
        addEqCondition(queryWrapper, "password_reset_at", searchRequest.getPasswordResetAt());
        addEqCondition(queryWrapper, "login_fail_count", searchRequest.getLoginFailCount());
        addEqCondition(queryWrapper, "locked_at", searchRequest.getLockedAt());
        addEqCondition(queryWrapper, "deleted", searchRequest.getDeleted());
        addEqCondition(queryWrapper, "version", searchRequest.getVersion());

        // 3. 时间范围查询
        handleTimeQuery(queryWrapper, "created_at",
                searchRequest.getCreatedAt(),
                searchRequest.getCreateTimeStart(),
                searchRequest.getCreateTimeEnd());

        handleTimeQuery(queryWrapper, "updated_at",
                searchRequest.getUpdatedAt(),
                searchRequest.getUpdateTimeStart(),
                searchRequest.getUpdateTimeEnd());

        return userMapper.selectPage(new Page<>(pageRequest.getPageNum(), pageRequest.getPageSize()), queryWrapper);
    }

    // 辅助方法：添加模糊查询条件
    private void addLikeCondition(QueryWrapper<User> queryWrapper, String column, String value) {
        if (StringUtils.hasText(value)) {
            queryWrapper.like(column, value.trim());
        }
    }

    // 辅助方法：添加精确查询条件
    private <T> void addEqCondition(QueryWrapper<User> queryWrapper, String column, T value) {
        if (value != null) {
            queryWrapper.eq(column, value);
        }
    }

    /**
     * 处理时间查询条件
     *
     * @param queryWrapper 查询条件包装器
     * @param column 数据库列名
     * @param exactTime 精确时间
     * @param startTime 开始时间
     * @param endTime 结束时间
     */
    private void handleTimeQuery(QueryWrapper<User> queryWrapper, String column,
                                 LocalDateTime exactTime,
                                 LocalDateTime startTime,
                                 LocalDateTime endTime) {
        if (exactTime != null) {
            // 精确时间查询
            queryWrapper.eq(column, exactTime);
        } else {
            // 范围时间查询
            if (startTime != null && endTime != null) {
                queryWrapper.between(column, startTime, endTime);
            } else if (startTime != null) {
                queryWrapper.ge(column, startTime);
            } else if (endTime != null) {
                queryWrapper.le(column, endTime);
            }
        }
    }

    /**
     * 构建查询条件
     */
    private QueryWrapper<User> buildExcelExportQueryWrapper(UserExportRequest request) {
        QueryWrapper<User> wrapper = new QueryWrapper<>();

        // 注意，其它字段由 UserExportService 中的 EasyExcel 自动代理了
        if (request.getStatus() != null) {
            wrapper.eq("status", request.getStatus());
        }
        return wrapper;
    }
}
