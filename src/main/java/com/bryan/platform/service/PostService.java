package com.bryan.platform.service;

import com.bryan.platform.model.entity.Comment;
import com.bryan.platform.model.entity.Post;
import com.bryan.platform.dao.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID; // 用于生成评论 ID

/**
 * ClassName: PostServiceImpl
 * Package: com.bryan.platform.service.impl
 * Description: 博文服务实现类，适配 MongoDB。
 * Author: Bryan Long
 * Create: 2025/6/20
 * Version: v1.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PostService {

    private final PostRepository postRepository;

    /**
     * 创建新博文。
     *
     * @param post 博文实体
     * @param authorId 作者用户ID (MySQL User 的 ID)
     * @param authorName 作者名称 (MySQL User 的 username)
     * @return 创建后的博文实体
     */
    public Post createPost(Post post, Long authorId, String authorName) {
        // 设置作者 ID 和作者名称
        post.setAuthorId(authorId);
        post.setAuthorName(authorName);
        post.setSlug(generateSlug(post.getTitle()));
        post.setStatus(Post.PostStatus.DRAFT); // 默认草稿状态
        // createdAt 和 updatedAt 由 Spring Data MongoDB 自动填充，但为了明确性也可以手动设置
        // post.setCreatedAt(LocalDateTime.now());
        // post.setUpdatedAt(LocalDateTime.now());
        // 初始化统计数据
        if (post.getStats() == null) {
            post.setStats(new Post.PostStats());
        }
        return postRepository.save(post);
    }

    /**
     * 更新博文。
     *
     * @param id          博文ID (String 类型)
     * @param postUpdates 包含更新内容的博文实体
     * @param currentUserId 当前操作用户ID，用于权限校验
     * @param isAdmin 当前操作用户是否为管理员，用于权限校验
     * @return 更新后的博文实体
     * @throws RuntimeException 如果博文不存在或无权限
     */
    public Post updatePost(String id, Post postUpdates, Long currentUserId, boolean isAdmin) {
        return postRepository.findById(id)
                .map(existingPost -> {
                    // 权限校验：只有管理员或博文作者可以更新
                    if (!isAdmin && !existingPost.getAuthorId().equals(currentUserId)) {
                        throw new RuntimeException("Unauthorized: You are not the author of this post.");
                    }

                    if (postUpdates.getTitle() != null) {
                        existingPost.setTitle(postUpdates.getTitle());
                        existingPost.setSlug(generateSlug(postUpdates.getTitle())); // 标题更新，slug也更新
                    }
                    if (postUpdates.getContent() != null) {
                        existingPost.setContent(postUpdates.getContent());
                    }
                    if (postUpdates.getTags() != null) { // 直接设置 List<String> 标签
                        existingPost.setTags(postUpdates.getTags());
                    }
                    if (postUpdates.getStatus() != null) {
                        existingPost.setStatus(postUpdates.getStatus());
                    }
                    if (postUpdates.getFeaturedImage() != null) {
                        existingPost.setFeaturedImage(postUpdates.getFeaturedImage());
                    }
                    // updatedAt 由 Spring Data MongoDB 自动填充
                    return postRepository.save(existingPost);
                })
                .orElseThrow(() -> new RuntimeException("Post not found with id: " + id));
    }

    /**
     * 删除博文。
     *
     * @param id 博文ID (String 类型)
     * @param currentUserId 当前操作用户ID，用于权限校验
     * @param isAdmin 当前操作用户是否为管理员，用于权限校验
     * @throws RuntimeException 如果博文不存在或无权限
     */
    public void deletePost(String id, Long currentUserId, boolean isAdmin) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found with id: " + id));

        // 权限校验：只有管理员或博文作者可以删除
        if (!isAdmin && !post.getAuthorId().equals(currentUserId)) {
            throw new RuntimeException("Unauthorized: You are not the author of this post.");
        }
        postRepository.deleteById(id);
    }

    /**
     * 根据 Slug 获取博文。
     *
     * @param slug 博文的唯一标识符
     * @return 博文实体
     * @throws RuntimeException 如果博文不存在
     */
    public Post getPostBySlug(String slug) {
        return postRepository.findBySlug(slug)
                .orElseThrow(() -> new RuntimeException("Post not found with slug: " + slug));
    }

    /**
     * 根据ID获取博文。
     *
     * @param id 博文ID
     * @return 博文实体
     * @throws RuntimeException 如果博文不存在
     */
    public Post getPostById(String id) {
        return postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found with id: " + id));
    }

    /**
     * 获取所有已发布的博文列表（分页）。
     *
     * @param pageable 分页信息
     * @return 分页的博文列表
     */
    public Page<Post> getPublishedPosts(Pageable pageable) {
        return postRepository.findByStatusOrderByCreatedAtDesc(Post.PostStatus.PUBLISHED, pageable);
    }

    /**
     * 为博文添加评论。
     *
     * @param postId 博文ID (String 类型)
     * @param comment 评论实体（只包含内容，作者信息由服务层注入）
     * @param authorId 评论作者ID (MySQL User 的 ID)
     * @param authorName 评论作者名称 (MySQL User 的 username)
     * @return 添加评论后的 Post 实体（包含新的评论）
     * @throws RuntimeException 如果博文不存在
     */
    public Post addComment(String postId, Comment comment, Long authorId, String authorName) {
        // 验证博文是否存在
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found with id: " + postId));

        // 设置评论的关联信息和唯一ID
        comment.setId(UUID.randomUUID().toString()); // 为内嵌评论生成唯一ID
        comment.setAuthorId(authorId); // 设置评论作者 ID
        comment.setAuthorName(authorName); // 设置评论作者名称
        comment.setCreatedAt(LocalDateTime.now());

        post.getComments().add(comment); // 将评论添加到内嵌列表中
        return postRepository.save(post); // 保存更新后的 Post 文档
    }

    /**
     * 增加博文浏览量。
     *
     * @param postId 博文ID (String 类型)
     */
    public void incrementViews(String postId) {
        postRepository.findById(postId).ifPresent(post -> {
            post.getStats().setViews(post.getStats().getViews() + 1);
            postRepository.save(post); // 保存更新后的 Post 文档
        });
    }

    /**
     * 实现全文搜索。
     *
     * @param keyword 搜索关键词
     * @return 匹配的博文列表
     */
    public List<Post> fullTextSearch(String keyword) {
        // 利用 MongoDB 的文本索引
        return postRepository.fullTextSearch(keyword);
    }

    /**
     * 推荐博文（基于标签）。
     *
     * @param currentPostId 当前博文ID (String 类型)
     * @param limit         推荐数量限制
     * @return 推荐的博文列表
     */
    public List<Post> recommendPosts(String currentPostId, int limit) {
        Post currentPost = postRepository.findById(currentPostId)
                .orElseThrow(() -> new RuntimeException("Current post not found with id: " + currentPostId));

        List<String> tags = currentPost.getTags(); // 获取当前博文的标签列表
        if (tags.isEmpty()) {
            return Collections.emptyList(); // 如果没有标签，不进行推荐
        }

        // 使用分页查询，只获取指定数量的推荐博文
        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "createdAt"));
        return postRepository.findByTagsInAndStatusOrderByCreatedAtDesc(
                tags,
                Post.PostStatus.PUBLISHED,
                pageable
        ).getContent(); // getContent() 获取 Page 中的实际内容
    }

    /**
     * 删除博文中的评论。
     *
     * @param postId    博文ID
     * @param commentId 评论ID
     * @param currentUserId 当前操作用户ID，用于权限校验
     * @param isAdmin 当前操作用户是否为管理员，用于权限校验
     * @return 更新后的博文实体
     * @throws RuntimeException 如果博文或评论不存在或无权限
     */
    public Post deleteComment(String postId, String commentId, Long currentUserId, boolean isAdmin) {
        return postRepository.findById(postId)
                .map(post -> {
                    // 找到要删除的评论
                    Comment commentToRemove = post.getComments().stream()
                            .filter(comment -> comment.getId().equals(commentId))
                            .findFirst()
                            .orElseThrow(() -> new RuntimeException("Comment not found with id: " + commentId + " in post: " + postId));

                    // 权限校验：只有管理员或评论作者可以删除评论
                    if (!isAdmin && !commentToRemove.getAuthorId().equals(currentUserId)) {
                        throw new RuntimeException("Unauthorized: You are not the author of this comment.");
                    }

                    post.getComments().remove(commentToRemove); // 移除评论
                    return postRepository.save(post);
                })
                .orElseThrow(() -> new RuntimeException("Post not found with id: " + postId));
    }


    /**
     * 生成博文的 Slug。
     *
     * @param title 博文标题
     * @return 生成的 Slug 字符串
     */
    private String generateSlug(String title) {
        return title;
    }
}