package com.bryan.platform.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bryan.platform.model.entity.User;
import com.bryan.platform.model.entity.UserFollow;

/**
 * ClassName: UserFollowService
 * Package: com.bryan.platform.service
 * Description:
 * Author: Bryan Long
 * Create: 2025/6/23 - 18:53
 * Version: v1.0
 */
public interface UserFollowService {

    Boolean followUser(Long followerId, Long followingId);

    Boolean unfollowUser(Long followerId, Long followingId);

    Page<User> getFollowingUsers(Long userId, Integer pageNum, Integer pageSize);

    Page<User> getFollowerUsers(Long userId, Integer pageNum, Integer pageSize);

    Boolean isFollowing(Long followerId, Long followingId);
}
