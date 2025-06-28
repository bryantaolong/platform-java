package com.bryan.platform.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bryan.platform.dao.mapper.UserFollowMapper;
import com.bryan.platform.dao.mapper.UserMapper;
import com.bryan.platform.model.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * ClassName: UserFollowServiceImpl
 * Package: com.bryan.platform.service.impl
 * Description:
 * Author: Bryan Long
 * Create: 2025/6/23 - 18:57
 * Version: v1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserFollowService implements UserFollowService {

    private final UserFollowMapper userFollowMapper;
    private final UserMapper userMapper;

    @Override
    public Boolean followUser(Long followerId, Long followingId) {
        // 验证用户是否存在
        if (userMapper.selectById(followerId) == null) {
            throw new RuntimeException("当前用户不存在");
        }
        if (userMapper.selectById(followingId) == null) {
            throw new RuntimeException("被关注用户不存在");
        }

        // 检查是否已关注
        if (userFollowMapper.existsFollow(followerId, followingId) > 0) {
            throw new RuntimeException("您已经关注过该用户");
        }

        return userFollowMapper.insertFollow(followerId, followingId) > 0;
    }

    @Override
    public Boolean unfollowUser(Long followerId, Long followingId) {
        if (!isFollowing(followerId, followingId)) {
            throw new RuntimeException("您尚未关注该用户");
        }
        return userFollowMapper.deleteFollow(followerId, followingId) > 0;
    }

    @Override
    public Page<User> getFollowingUsers(Long userId, Integer pageNum, Integer pageSize) {
        if (userMapper.selectById(userId) == null) {
            throw new RuntimeException("用户不存在");
        }

        // 手动计算 offset = (pageNum - 1) * pageSize
        long offset = (long) (pageNum - 1) * pageSize;

        // 查询当前页数据
        List<User> records = userFollowMapper.selectFollowingList(userId, offset, (long) pageSize);
        // 查询总数
        long total = userFollowMapper.countFollowing(userId);

        // 创建并返回分页结果
        Page<User> page = new Page<>(pageNum, pageSize);
        return page.setRecords(records).setTotal(total);
    }

    @Override
    public Page<User> getFollowerUsers(Long userId, Integer pageNum, Integer pageSize) {
        if (userMapper.selectById(userId) == null) {
            throw new RuntimeException("用户不存在");
        }

        // 手动计算 offset = (pageNum - 1) * pageSize
        long offset = (long) (pageNum - 1) * pageSize;

        // 查询当前页数据
        List<User> records = userFollowMapper.selectFollowerList(userId, offset, (long) pageSize);
        // 查询总数
        long total = userFollowMapper.countFollowers(userId);

        // 创建并返回分页结果
        Page<User> page = new Page<>(pageNum, pageSize);
        return page.setRecords(records).setTotal(total);
    }

    @Override
    public Boolean isFollowing(Long followerId, Long followingId) {
        if (userMapper.selectById(followerId) == null) {
            throw new RuntimeException("未登录无法查看关注列表");
        }
        return userFollowMapper.existsFollow(followerId, followingId) > 0;
    }
}
