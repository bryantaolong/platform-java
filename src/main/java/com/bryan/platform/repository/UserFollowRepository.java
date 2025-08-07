package com.bryan.platform.repository;

import com.bryan.platform.model.entity.user.UserFollow;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * UserFollowRepository
 *
 * @author Bryan Long
 * @version 1.0
 * @since 2025/8/7
 */
@Repository
public interface UserFollowRepository extends JpaRepository<UserFollow, Long> {

    /* 根据关注者+被关注者+删除标记查询单条记录 */
    Optional<UserFollow> findByFollowerIdAndFollowingIdAndDeleted(Long followerId, Long followingId, Integer deleted);

    /* 分页：关注列表 */
    Page<UserFollow> findByFollowerIdAndDeletedOrderByCreateTimeDesc(Long followerId, Integer deleted, Pageable pageable);

    /* 分页：粉丝列表 */
    Page<UserFollow> findByFollowingIdAndDeletedOrderByCreateTimeDesc(Long followingId, Integer deleted, Pageable pageable);

    /* 统计关注数 */
    long countByFollowerIdAndDeleted(Long followerId, Integer deleted);

    /* 统计粉丝数 */
    long countByFollowingIdAndDeleted(Long followingId, Integer deleted);
}
