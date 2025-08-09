package com.bryan.platform.repository;

import com.bryan.platform.domain.entity.post.PostFavorite;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * PostFavoriteRepository
 *
 * @author Bryan Long
 */
@Repository
public interface PostFavoriteRepository extends JpaRepository<PostFavorite, Long> {

    /** 分页：用户收藏的博文 */
    Page<PostFavorite> findByUserIdAndDeletedOrderByCreateTimeDesc(Long userId, Integer deleted, Pageable pageable);

    /** 是否已收藏 */
    Optional<PostFavorite> findByUserIdAndPostIdAndDeleted(Long userId, String postId, Integer deleted);

    /** 统计收藏数 */
    long countByUserIdAndDeleted(Long userId, Integer deleted);
}
