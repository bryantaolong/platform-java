package com.bryan.platform.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bryan.platform.mapper.UserFollowMapper;
import com.bryan.platform.domain.entity.user.UserFollow;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

/**
 * UserFollowRepository
 *
 * @author Bryan Long
 */
@Repository
@RequiredArgsConstructor
public class UserFollowRepository {

    private final UserFollowMapper userFollowMapper;

    public UserFollow save(UserFollow userFollow) {
        userFollowMapper.insert(userFollow);
        return userFollow;
    }

    public UserFollow findById(Long id) {
        return userFollowMapper.selectById(id);
    }

    public Page<UserFollow> findAllByUserId(Long userId, long pageNum, long pageSize) {
        Page<UserFollow> followPage = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<UserFollow> wrapper = new LambdaQueryWrapper<UserFollow>()
                .eq(UserFollow::getFollowerId, userId);

        return  userFollowMapper.selectPage(followPage, wrapper);
    }

    public Boolean deleteByFollowerIdAndFollowingId(Long followingId, Long followerId) {
        LambdaQueryWrapper<UserFollow> wrapper = new LambdaQueryWrapper<UserFollow>()
                .eq(UserFollow::getFollowerId, followerId)
                .eq(UserFollow::getFollowingId, followingId);

        return userFollowMapper.delete(wrapper) > 0;
    }

    public Long count(Long userId) {
        LambdaQueryWrapper<UserFollow> wrapper = new LambdaQueryWrapper<UserFollow>()
                .eq(UserFollow::getFollowingId, userId);
        return userFollowMapper.selectCount(wrapper);
    }

    public Long countByFollowerIdAndFollowingId(Long followerId, Long followingId) {
        LambdaQueryWrapper<UserFollow> wrapper = new LambdaQueryWrapper<UserFollow>()
                .eq(UserFollow::getFollowerId, followerId)
                .eq(UserFollow::getFollowingId, followingId);
        return userFollowMapper.selectCount(wrapper);
    }
}
