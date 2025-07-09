package com.bryan.platform.service;

import com.bryan.platform.model.entity.Comment;
import com.bryan.platform.model.entity.Post;
import com.bryan.platform.dao.repository.PostRepository;
import com.bryan.platform.model.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils; // 引入 StringUtils

import java.time.LocalDateTime;
import java.util.ArrayList; // 确保导入，用于初始化评论列表
import java.util.Collections;
import java.util.List;
import java.util.Optional; // 引入 Optional
import java.util.UUID;
import java.util.stream.Collectors;

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
    private final UserFollowService userFollowService;

    /**
     * 根据 Slug 获取博文。
     *
     * @param slug 博文的唯一标识符
     * @return 博文实体
     * @throws RuntimeException 如果博文不存在
     */
    public Post getPostBySlug(String slug) {
        // 确保 slug 不为 null 或空，避免后端查询 null
        if (slug == null || slug.trim().isEmpty()) {
            throw new RuntimeException("Invalid slug: slug cannot be null or empty.");
        }
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
     * 根据作者ID获取博文。
     *
     * @param authorId 博文ID
     * @return 博文实体
     * @throws RuntimeException 如果博文不存在
     */
    public Page<Post> getPostsByAuthorId(Long authorId, Pageable pageable) {
        return postRepository.findByAuthorIdOrderByCreatedAtDesc(authorId, pageable);
    }

    /**
     * 根据作者ID列表和状态获取博文（分页）
     * 获取用户关注的人的博文（分页）
     */
    public Page<Post> getFollowingPosts(Long userId, Pageable pageable) {
        // 1. 获取当前用户关注的所有用户
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<User> followingUsers =
                userFollowService.getFollowingUsers(
                        userId,
                        pageable.getPageNumber() + 1, // MyBatis-Plus 页码从1开始
                        pageable.getPageSize()
                );

        // 2. 如果没有关注任何人，返回空分页
        if (followingUsers.getRecords().isEmpty()) {
            return new PageImpl<>(Collections.emptyList(), pageable, 0);
        }

        // 3. 提取关注用户的ID列表
        List<Long> followingIds = followingUsers.getRecords()
                .stream()
                .map(User::getId)
                .collect(Collectors.toList());

        // 4. 查询这些用户发布的已发布博文
        return postRepository.findByAuthorIdInAndStatusOrderByCreatedAtDesc(
                followingIds,
                Post.PostStatus.PUBLISHED,
                pageable
        );
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

        // **核心修复：生成唯一的 slug**
        post.setSlug(generateUniqueSlug(post.getTitle(), null)); // null 表示没有要排除的旧ID

        post.setStatus(Post.PostStatus.DRAFT); // 默认草稿状态
        // createdAt 和 updatedAt 由 Spring Data MongoDB 自动填充，但为了明确性也可以手动设置
        if (post.getCreatedAt() == null) { // 确保没有重复设置
            post.setCreatedAt(LocalDateTime.now());
        }
        post.setUpdatedAt(LocalDateTime.now());

        // 初始化统计数据、评论和标签列表，防止 NPE
        if (post.getStats() == null) {
            post.setStats(new Post.PostStats());
        }
        if (post.getComments() == null) { // 确保评论列表不为 null
            post.setComments(new ArrayList<>());
        }
        if (post.getTags() == null) { // 确保标签集合不为 null
            post.setTags(new ArrayList<>());
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

                    boolean titleChanged = false;
                    if (postUpdates.getTitle() != null && !postUpdates.getTitle().equals(existingPost.getTitle())) {
                        existingPost.setTitle(postUpdates.getTitle());
                        titleChanged = true;
                    }
                    if (postUpdates.getContent() != null) {
                        existingPost.setContent(postUpdates.getContent());
                    }
                    if (postUpdates.getTags() != null) {
                        existingPost.setTags(postUpdates.getTags());
                    }
                    if (postUpdates.getStatus() != null) {
                        existingPost.setStatus(postUpdates.getStatus());
                    }
                    if (postUpdates.getFeaturedImage() != null) {
                        existingPost.setFeaturedImage(postUpdates.getFeaturedImage());
                    }

                    // **核心修复：如果标题改变，重新生成 slug，并确保其唯一性**
                    if (titleChanged) {
                        existingPost.setSlug(generateUniqueSlug(existingPost.getTitle(), existingPost.getId()));
                    } else if (postUpdates.getSlug() != null && !postUpdates.getSlug().equals(existingPost.getSlug())) {
                        // 如果前端明确提供了新的 slug 并且与旧的不同，也更新并验证唯一性
                        existingPost.setSlug(generateUniqueSlug(postUpdates.getSlug(), existingPost.getId()));
                    } else if (StringUtils.isEmpty(existingPost.getSlug())) {
                        // 如果现有 slug 为空（针对旧数据），也要生成
                        existingPost.setSlug(generateUniqueSlug(existingPost.getTitle(), existingPost.getId()));
                    }

                    existingPost.setUpdatedAt(LocalDateTime.now()); // 更新时间
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

        // 确保评论列表已初始化
        if (post.getComments() == null) {
            post.setComments(new ArrayList<>());
        }
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
        if (tags == null || tags.isEmpty()) { // 检查 tags 是否为 null 或空
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
                    // 确保评论列表已初始化，否则查找会报错
                    if (post.getComments() == null) {
                        post.setComments(new ArrayList<>());
                    }
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
     * 生成一个唯一的 slug。
     *
     * @param title      博文标题或基础字符串
     * @param excludeId  要排除的博文ID (用于更新时避免和自身冲突)，如果是创建新博文则传 null
     * @return 唯一的 slug
     */
    private String generateUniqueSlug(String title, String excludeId) {
        // 1. 清理并标准化标题
        String baseSlug = title == null ? "post" : title.toLowerCase()
                .replaceAll("[^a-zA-Z0-9\\u4e00-\\u9fa5\\s-]", "") // 允许字母、数字、中文、空格、连字符
                .replaceAll("\\s+", "-")       // 将空格替换为连字符
                .replaceAll("-+", "-")         // 替换多个连字符为单个
                .trim();

        if (baseSlug.isEmpty()) { // 防止空标题生成空slug
            baseSlug = "post";
        }
        // 移除开头和结尾的连字符，以防万一
        if (baseSlug.startsWith("-")) {
            baseSlug = baseSlug.substring(1);
        }
        if (baseSlug.endsWith("-")) {
            baseSlug = baseSlug.substring(0, baseSlug.length() - 1);
        }
        if (baseSlug.isEmpty()) { // 再次检查是否为空，如果只包含特殊字符导致清理后为空
            baseSlug = "untitled-post";
        }


        String uniqueSlug = baseSlug;
        int counter = 0;
        // 2. 检查唯一性并添加后缀
        while (true) {
            Optional<Post> existingPost = postRepository.findBySlug(uniqueSlug);
            if (existingPost.isPresent()) {
                // 如果找到现有博文，且其ID不是当前正在更新的博文ID (excludeId)
                if (excludeId == null || !existingPost.get().getId().equals(excludeId)) {
                    counter++;
                    uniqueSlug = baseSlug + "-" + counter;
                } else {
                    // 找到的博文就是当前正在更新的博文，说明 slug 唯一
                    break;
                }
            } else {
                // 没有找到同 slug 的博文，说明 slug 唯一
                break;
            }
        }
        return uniqueSlug;
    }
}