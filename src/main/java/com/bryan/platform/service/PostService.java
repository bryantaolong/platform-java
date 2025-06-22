package com.bryan.platform.service; // 假设 PostService 接口定义在此包

import com.bryan.platform.model.entity.Comment;
import com.bryan.platform.model.entity.Post;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * ClassName: PostService
 * Package: com.bryan.platform.service
 * Description: 博文服务接口。
 * 定义了博文相关的业务操作。
 * Author: Bryan Long
 * Create: 2025/6/20
 * Version: v1.0
 */
public interface PostService {

    /**
     * 创建新博文。
     * @param post 博文实体，不包含作者信息
     * @param authorId 作者用户ID (MySQL User 的 ID)
     * @param authorName 作者名称 (MySQL User 的 username)
     * @return 创建后的博文实体
     */
    Post createPost(Post post, Long authorId, String authorName);

    /**
     * 更新博文。
     * @param id 博文ID (String 类型)
     * @param postUpdates 包含更新内容的博文实体
     * @param currentUserId 当前操作用户ID，用于权限校验
     * @param isAdmin 当前操作用户是否为管理员，用于权限校验
     * @return 更新后的博文实体
     */
    Post updatePost(String id, Post postUpdates, Long currentUserId, boolean isAdmin);

    /**
     * 删除博文。
     * @param id 博文ID (String 类型)
     * @param currentUserId 当前操作用户ID，用于权限校验
     * @param isAdmin 当前操作用户是否为管理员，用于权限校验
     */
    void deletePost(String id, Long currentUserId, boolean isAdmin);

    /**
     * 根据 Slug 获取博文。
     * @param slug 博文的唯一标识符
     * @return 博文实体
     */
    Post getPostBySlug(String slug);

    /**
     * 根据ID获取博文。
     * @param id 博文ID
     * @return 博文实体
     */
    Post getPostById(String id);

    /**
     * 获取所有已发布的博文列表（分页）。
     * @param pageable 分页信息
     * @return 分页的博文列表
     */
    Page<Post> getPublishedPosts(Pageable pageable);


    /**
     * 为博文添加评论。
     * @param postId 博文ID (String 类型)
     * @param comment 评论实体（只包含内容，作者信息由服务层注入）
     * @param authorId 评论作者ID (MySQL User 的 ID)
     * @param authorName 评论作者名称 (MySQL User 的 username)
     * @return 添加评论后的 Post 实体（包含新的评论）
     */
    Post addComment(String postId, Comment comment, Long authorId, String authorName);

    /**
     * 增加博文浏览量。
     * @param postId 博文ID (String 类型)
     */
    void incrementViews(String postId);

    /**
     * 实现全文搜索。
     * @param keyword 搜索关键词
     * @return 匹配的博文列表
     */
    List<Post> fullTextSearch(String keyword);

    /**
     * 推荐博文（基于标签）。
     * @param currentPostId 当前博文ID (String 类型)
     * @param limit 推荐数量限制
     * @return 推荐的博文列表
     */
    List<Post> recommendPosts(String currentPostId, int limit);

    /**
     * 删除博文中的评论。
     * @param postId 博文ID
     * @param commentId 评论ID
     * @param currentUserId 当前操作用户ID，用于权限校验
     * @param isAdmin 当前操作用户是否为管理员，用于权限校验
     * @return 更新后的博文实体
     */
    Post deleteComment(String postId, String commentId, Long currentUserId, boolean isAdmin);
}