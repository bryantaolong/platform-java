package com.bryan.platform.service.user;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bryan.platform.common.exception.BusinessException;
import com.bryan.platform.dao.mapper.UserFollowMapper;
import com.bryan.platform.dao.mapper.UserMapper;
import com.bryan.platform.model.entity.user.User;
import com.bryan.platform.model.entity.user.UserFollow;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

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
        if (isFollowing(followerId, followingId)) {
            throw new BusinessException("您已经关注过该用户");
        }

        // 3. 插入关注关系
        UserFollow userFollow = UserFollow.builder()
                .followerId(followerId)
                .followingId(followingId)
                .build();
        return userFollowMapper.insert(userFollow) > 0;
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
        LambdaQueryWrapper<UserFollow> wrapper = new LambdaQueryWrapper<UserFollow>()
                .eq(UserFollow::getFollowerId, followerId)
                .eq(UserFollow::getFollowingId, followingId);
        return userFollowMapper.delete(wrapper) > 0;
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

        // 2. 查询关注关系
        Page<UserFollow> followPage = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<UserFollow> wrapper = new LambdaQueryWrapper<UserFollow>()
                .eq(UserFollow::getFollowerId, userId);
        userFollowMapper.selectPage(followPage, wrapper);

        // 3. 获取被关注的用户ID列表
        List<Long> followingIds = followPage.getRecords().stream()
                .map(UserFollow::getFollowingId)
                .collect(Collectors.toList());

        // 4. 查询用户信息
        List<User> users = followingIds.isEmpty() ?
                List.of() :
                userMapper.selectBatchIds(followingIds);

        // 5. 返回分页结果
        Page<User> resultPage = new Page<>(pageNum, pageSize, followPage.getTotal());
        return resultPage.setRecords(users);
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

        // 2. 查询粉丝关系
        Page<UserFollow> followPage = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<UserFollow> wrapper = new LambdaQueryWrapper<UserFollow>()
                .eq(UserFollow::getFollowingId, userId);
        userFollowMapper.selectPage(followPage, wrapper);

        // 3. 获取粉丝用户ID列表
        List<Long> followerIds = followPage.getRecords().stream()
                .map(UserFollow::getFollowerId)
                .collect(Collectors.toList());

        // 4. 查询用户信息
        List<User> users = followerIds.isEmpty() ?
                List.of() :
                userMapper.selectBatchIds(followerIds);

        // 5. 返回分页结果
        Page<User> resultPage = new Page<>(pageNum, pageSize, followPage.getTotal());
        return resultPage.setRecords(users);
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
        // 校验当前用户是否存在
        if (userMapper.selectById(followerId) == null) {
            throw new BusinessException("未登录无法查看关注状态");
        }

        LambdaQueryWrapper<UserFollow> wrapper = new LambdaQueryWrapper<UserFollow>()
                .eq(UserFollow::getFollowerId, followerId)
                .eq(UserFollow::getFollowingId, followingId);
        return userFollowMapper.selectCount(wrapper) > 0;
    }

    public Long countFollowing(Long userId) {
        LambdaQueryWrapper<UserFollow> wrapper = new LambdaQueryWrapper<UserFollow>()
                .eq(UserFollow::getFollowerId, userId);
        return userFollowMapper.selectCount(wrapper);
    }

    public Long countFollowers(Long userId) {
        LambdaQueryWrapper<UserFollow> wrapper = new LambdaQueryWrapper<UserFollow>()
                .eq(UserFollow::getFollowingId, userId);
        return userFollowMapper.selectCount(wrapper);
    }
}
