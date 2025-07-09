package com.bryan.platform.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bryan.platform.common.exception.BusinessException;
import com.bryan.platform.dao.mapper.UserFollowMapper;
import com.bryan.platform.dao.mapper.UserMapper;
import com.bryan.platform.model.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 用户关注服务类。
 * 提供用户关注/取关、查询关注列表和粉丝列表、是否关注判断等功能。
 *
 * @author Bryan Long
 * @version v1.1（支持事务 + 业务异常）
 * @since 2025/6/23
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserFollowService {

    private final UserFollowMapper userFollowMapper;
    private final UserMapper userMapper;

    /**
     * 当前用户关注另一个用户
     *
     * @param followerId 关注者 ID
     * @param followingId 被关注者 ID
     * @return 是否成功
     * @throws BusinessException 若用户不存在或已关注
     */
    @Transactional(rollbackFor = Exception.class)
    public Boolean followUser(Long followerId, Long followingId) {
        // 1. 校验用户是否存在
        if (userMapper.selectById(followerId) == null) {
            throw new BusinessException("当前用户不存在");
        }
        if (userMapper.selectById(followingId) == null) {
            throw new BusinessException("被关注用户不存在");
        }

        // 2. 检查是否已关注
        if (userFollowMapper.existsFollow(followerId, followingId) > 0) {
            throw new BusinessException("您已经关注过该用户");
        }

        // 3. 插入关注关系
        return userFollowMapper.insertFollow(followerId, followingId) > 0;
    }

    /**
     * 当前用户取消关注另一个用户
     *
     * @param followerId 关注者 ID
     * @param followingId 被关注者 ID
     * @return 是否成功
     * @throws BusinessException 若未关注该用户
     */
    @Transactional(rollbackFor = Exception.class)
    public Boolean unfollowUser(Long followerId, Long followingId) {
        // 1. 检查是否已关注
        if (!isFollowing(followerId, followingId)) {
            throw new BusinessException("您尚未关注该用户");
        }

        // 2. 删除关注关系
        return userFollowMapper.deleteFollow(followerId, followingId) > 0;
    }

    /**
     * 获取当前用户关注的用户列表（分页）
     *
     * @param userId 用户 ID
     * @param pageNum 当前页码（从 1 开始）
     * @param pageSize 每页大小
     * @return 分页的用户列表
     * @throws BusinessException 若用户不存在
     */
    public Page<User> getFollowingUsers(Long userId, Integer pageNum, Integer pageSize) {
        // 1. 校验用户是否存在
        if (userMapper.selectById(userId) == null) {
            throw new BusinessException("用户不存在");
        }

        // 2. 计算偏移量
        long offset = (long) (pageNum - 1) * pageSize;

        // 3. 查询数据
        List<User> records = userFollowMapper.selectFollowingList(userId, offset, (long) pageSize);
        long total = userFollowMapper.countFollowing(userId);

        // 4. 返回分页结果
        Page<User> page = new Page<>(pageNum, pageSize);
        return page.setRecords(records).setTotal(total);
    }

    /**
     * 获取关注当前用户的用户列表（分页）
     *
     * @param userId 用户 ID
     * @param pageNum 当前页码（从 1 开始）
     * @param pageSize 每页大小
     * @return 分页的粉丝用户列表
     * @throws BusinessException 若用户不存在
     */
    public Page<User> getFollowerUsers(Long userId, Integer pageNum, Integer pageSize) {
        // 1. 校验用户是否存在
        if (userMapper.selectById(userId) == null) {
            throw new BusinessException("用户不存在");
        }

        // 2. 计算偏移量
        long offset = (long) (pageNum - 1) * pageSize;

        // 3. 查询数据
        List<User> records = userFollowMapper.selectFollowerList(userId, offset, (long) pageSize);
        long total = userFollowMapper.countFollowers(userId);

        // 4. 返回分页结果
        Page<User> page = new Page<>(pageNum, pageSize);
        return page.setRecords(records).setTotal(total);
    }

    /**
     * 判断当前用户是否已关注指定用户
     *
     * @param followerId 当前用户 ID
     * @param followingId 目标用户 ID
     * @return true 表示已关注，false 表示未关注
     * @throws BusinessException 若当前用户不存在
     */
    public Boolean isFollowing(Long followerId, Long followingId) {
        // 1. 校验当前用户是否存在
        if (userMapper.selectById(followerId) == null) {
            throw new BusinessException("未登录无法查看关注状态");
        }

        // 2. 检查是否存在关注关系
        return userFollowMapper.existsFollow(followerId, followingId) > 0;
    }
}
