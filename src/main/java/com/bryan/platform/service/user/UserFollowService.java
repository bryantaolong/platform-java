package com.bryan.platform.service.user;

import com.bryan.platform.domain.entity.user.SysUser;
import com.bryan.platform.domain.response.PageResult;
import com.bryan.platform.exception.BusinessException;
import com.bryan.platform.domain.entity.user.UserFollow;
import com.bryan.platform.mapper.UserFollowMapper;
import com.bryan.platform.mapper.UserMapper;
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
    public int followUser(Long followerId, Long followingId) {
        // 1. 校验双方用户存在
        if (userMapper.selectById(followerId) == null) {
            throw new BusinessException("当前用户不存在");
        }
        if (userMapper.selectById(followingId) == null) {
            throw new BusinessException("被关注用户不存在");
        }

        // 2. 是否已关注
        if (this.isFollowing(followerId, followingId)) {
            throw new BusinessException("您已经关注过该用户");
        }

        // 3. 插入
        UserFollow uf = UserFollow.builder()
                .followerId(followerId)
                .followingId(followingId)
                .build();
        return userFollowMapper.insert(uf);
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
    public int unfollowUser(Long followerId, Long followingId) {
        // 检查是否已关注
        if (this.isFollowing(followerId, followingId)) {
            throw new BusinessException("您尚未关注该用户");
        }

        return userFollowMapper.deleteByFollowerIdAndFollowingId(followerId, followingId);
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
    public PageResult<SysUser> getFollowingUsers(Long userId,
                                                 Integer pageNum,
                                                 Integer pageSize) {
        if (userMapper.selectById(userId) == null) {
            throw new BusinessException("用户不存在");
        }
        long offset = (long) (pageNum - 1) * pageSize;

        List<UserFollow> follows = userFollowMapper.selectPageByFollowerId(userId, offset, pageSize);
        long total = userFollowMapper.countByFollowerId(userId);

        List<Long> followingIds = follows.stream()
                .map(UserFollow::getFollowingId)
                .toList();
        List<SysUser> users = followingIds.isEmpty()
                ? List.of()
                : userMapper.selectByIdList(followingIds);

        return PageResult.of(users, total, pageNum, pageSize);
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
    public PageResult<SysUser> getFollowerUsers(Long userId,
                                                Integer pageNum,
                                                Integer pageSize) {
        if (userMapper.selectById(userId) == null) {
            throw new BusinessException("用户不存在");
        }
        long offset = (long) (pageNum - 1) * pageSize;

        List<UserFollow> follows = userFollowMapper.selectPageByFollowingId(userId, offset, pageSize);
        long total = userFollowMapper.countByFollowingId(userId);

        List<Long> followerIds = follows.stream()
                .map(UserFollow::getFollowerId)
                .toList();
        List<SysUser> users = followerIds.isEmpty()
                ? List.of()
                : userMapper.selectByIdList(followerIds);

        return PageResult.of(users, total, pageNum, pageSize);
    }

    /**
     * 判断当前用户是否已关注指定用户
     *
     * @param followerId 当前用户 ID
     * @param followingId 目标用户 ID
     * @return true 表示已关注，false 表示未关注
     * @throws BusinessException 若当前用户不存在
     */
    public boolean isFollowing(Long followerId, Long followingId) {
        return userFollowMapper.countByFollowerIdAndFollowingId(followerId, followingId) > 0;
    }

    public long countFollowing(Long userId) {
        return userFollowMapper.countByFollowerId(userId);
    }

    public long countFollowers(Long userId) {
        return userFollowMapper.countByFollowingId(userId);
    }
}
