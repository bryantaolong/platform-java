package com.bryan.platform.service.user;

import com.bryan.platform.exception.BusinessException;
import com.bryan.platform.domain.entity.user.User;
import com.bryan.platform.domain.entity.user.UserFollow;
import com.bryan.platform.repository.UserFollowRepository;
import com.bryan.platform.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户关注服务类。
 * 提供用户关注/取关、查询关注列表和粉丝列表、是否关注判断等功能。
 *
 * @author Bryan Long
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class UserFollowService {

    private final UserFollowRepository userFollowRepository;
    private final UserRepository userRepository;

    public Boolean followUser(Long followerId, Long followingId) {
        // 1. 用户存在性校验
        if (!userRepository.existsById(followerId)) {
            throw new BusinessException("当前用户不存在");
        }
        if (!userRepository.existsById(followingId)) {
            throw new BusinessException("被关注用户不存在");
        }

        // 2. 幂等：已关注则抛错
        if (userFollowRepository.findByFollowerIdAndFollowingIdAndDeleted(followerId, followingId, 0).isPresent()) {
            throw new BusinessException("您已经关注过该用户");
        }

        UserFollow uf = UserFollow.builder()
                .followerId(followerId)
                .followingId(followingId)
                .deleted(0)
                .build();
        userFollowRepository.save(uf);
        return true;
    }

    public Boolean unfollowUser(Long followerId, Long followingId) {
        // 先查出未被逻辑删除的记录
        UserFollow follow = userFollowRepository
                .findByFollowerIdAndFollowingIdAndDeleted(followerId, followingId, 0)
                .orElseThrow(() -> new BusinessException("您尚未关注该用户"));

        // 触发 @SQLDelete 的 UPDATE
        userFollowRepository.delete(follow);
        return true;
    }

    /**
     * 关注列表：先查关系表，再批量查用户
     */
    public Page<User> getFollowingUsers(Long userId, Pageable pageable) {
        if (!userRepository.existsById(userId)) {
            throw new BusinessException("用户不存在");
        }
        Page<UserFollow> page = userFollowRepository.findByFollowerIdAndDeletedOrderByCreateTimeDesc(userId, 0, pageable);
        List<Long> userIds = page.getContent()
                .stream()
                .map(UserFollow::getFollowingId)
                .collect(Collectors.toList());
        List<User> users = userIds.isEmpty() ? List.of() : userRepository.findAllById(userIds);
        return new PageImpl<>(users, pageable, page.getTotalElements());
    }

    /**
     * 粉丝列表：同理
     */
    public Page<User> getFollowerUsers(Long userId, Pageable pageable) {
        if (!userRepository.existsById(userId)) {
            throw new BusinessException("用户不存在");
        }
        Page<UserFollow> page = userFollowRepository.findByFollowingIdAndDeletedOrderByCreateTimeDesc(userId, 0, pageable);
        List<Long> userIds = page.getContent()
                .stream()
                .map(UserFollow::getFollowerId)
                .collect(Collectors.toList());
        List<User> users = userIds.isEmpty() ? List.of() : userRepository.findAllById(userIds);
        return new PageImpl<>(users, pageable, page.getTotalElements());
    }

    public Boolean isFollowing(Long followerId, Long followingId) {
        return userFollowRepository.findByFollowerIdAndFollowingIdAndDeleted(followerId, followingId, 0).isPresent();
    }

    public long countFollowing(Long userId) {
        return userFollowRepository.countByFollowerIdAndDeleted(userId, 0);
    }

    public long countFollowers(Long userId) {
        return userFollowRepository.countByFollowingIdAndDeleted(userId, 0);
    }
}
