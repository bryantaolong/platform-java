package com.bryan.platform.service;

import com.bryan.platform.model.entity.Post;
import com.bryan.platform.model.entity.PostFavorite;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable; // Spring Data 的 Pageable 接口

/**
 * ClassName: PostFavoriteService
 * Package: com.bryan.platform.service
 * Description: 博文收藏服务接口。
 * 定义了与用户收藏博文相关的核心业务操作。
 * Author: Bryan Long
 * Create: 2025/6/22 - 16:22
 * Version: v1.0
 */
public interface PostFavoriteService {

    /**
     * 根据用户ID获取其收藏的博文列表（分页）。
     *
     * @param userId   用户的ID。
     * @param pageable 分页信息。
     * @return 该用户收藏的博文分页列表。
     */
    Page<Post> getFavoritePostsByUserId(Long userId, Pageable pageable);

    /**
     * 添加博文到用户的收藏列表。
     *
     * @param userId 收藏用户的ID。
     * @param postId 被收藏博文的ID (MongoDB ID)。
     * @return 成功添加的收藏记录数 (通常为 1)。
     */
    int addFavorite(Long userId, String postId);

    /**
     * 根据收藏记录的ID删除收藏。
     *
     * @param postFavoriteId 收藏记录的唯一ID。
     * @return 成功删除的记录数 (通常为 1)。
     */
    int deleteFavoriteById(String postFavoriteId);

    /**
     * 根据用户ID和博文ID删除收藏记录（用于取消收藏）。
     *
     * @param userId 收藏用户的ID。
     * @param postId 被取消收藏博文的ID (MongoDB ID)。
     * @return 成功删除的记录数 (通常为 1)。
     */
    int deleteFavoriteByUserIdAndPostId(Long userId, String postId);

    /**
     * 检查当前用户是否已收藏指定博文。
     * 该方法通常在控制器中调用，通过 SecurityContext 获取当前用户ID。
     *
     * @param userId 要检查的用户ID
     * @param postId 要检查的博文ID (MongoDB ID)。
     * @return 如果已收藏返回 true，否则返回 false。
     */
    Boolean checkFavorite(Long userId, String postId);
}
