package com.bryan.platform.service.post;

import com.bryan.platform.domain.entity.Comment;
import com.bryan.platform.domain.entity.post.Post;
import com.bryan.platform.repository.PostRepository;
import com.bryan.platform.domain.entity.user.User;
import com.bryan.platform.service.user.UserFollowService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 博文服务类（适配 MongoDB）
 * 提供博文的增删改查、评论、推荐、浏览统计等功能
 *
 * @author Bryan Long
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PostService {

    private final PostRepository postRepository;
    private final UserFollowService userFollowService;

    /**
     * 获取所有博文（管理员专用，支持分页和排序）
     *
     * @param pageable 分页参数
     * @return 所有博文分页结果
     */
    public Page<Post> getAllPosts(Pageable pageable) {
        return postRepository.findAll(pageable);
    }

    /**
     * 获取所有已发布博文（分页）
     *
     * @param pageable 分页参数
     * @return 博文分页结果
     */
    public Page<Post> getPublishedPosts(Pageable pageable) {
        return postRepository.findByStatusOrderByCreatedAtDesc(Post.PostStatus.PUBLISHED, pageable);
    }

    /**
     * 根据 ID 获取博文
     *
     * @param id 博文 ID
     * @return 博文实体
     * @throws RuntimeException 如果博文不存在
     */
    public Post getPostById(String id) {
        return postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found with id: " + id));
    }

    /**
     * 根据 slug 获取博文
     *
     * @param slug 博文唯一标识符
     * @return 博文实体
     * @throws RuntimeException 如果博文不存在或 slug 非法
     */
    public Post getPostBySlug(String slug) {
        // 1. 校验 slug 不为空
        if (slug == null || slug.trim().isEmpty()) {
            throw new RuntimeException("Invalid slug: slug cannot be null or empty.");
        }

        // 2. 查询博文
        return postRepository.findBySlug(slug)
                .orElseThrow(() -> new RuntimeException("Post not found with slug: " + slug));
    }

    /**
     * 获取指定作者的博文（分页）
     *
     * @param authorId 作者用户 ID
     * @param pageable 分页信息
     * @return 博文分页结果
     */
    public Page<Post> getPostsByAuthorId(Long authorId, Pageable pageable) {
        return postRepository.findByAuthorIdOrderByCreatedAtDesc(authorId, pageable);
    }

    /**
     * 获取当前用户关注用户的博文（分页）
     *
     * @param userId 当前用户 ID
     * @param pageable 分页信息
     * @return 关注用户的博文分页结果
     */
    public Page<Post> getFollowingPosts(Long userId, Pageable pageable) {
        // 1. 获取关注的用户列表
        var followingUsers = userFollowService.getFollowingUsers(
                userId,
                pageable.getPageNumber() + 1,
                pageable.getPageSize()
        );

        // 2. 若无关注用户，返回空分页
        if (followingUsers.getRecords().isEmpty()) {
            return new PageImpl<>(Collections.emptyList(), pageable, 0);
        }

        // 3. 获取被关注用户的 ID 列表
        List<Long> followingIds = followingUsers.getRecords()
                .stream()
                .map(User::getId)
                .collect(Collectors.toList());

        // 4. 查询对应用户的已发布博文
        return postRepository.findByAuthorIdInAndStatusOrderByCreatedAtDesc(
                followingIds,
                Post.PostStatus.PUBLISHED,
                pageable
        );
    }

    /**
     * 创建新博文
     *
     * @param post 博文实体
     * @param authorId 作者 ID
     * @param authorName 作者名称
     * @return 保存后的博文
     */
    public Post createPost(Post post, Long authorId, String authorName) {
        // 1. 设置作者信息
        post.setAuthorId(authorId);
        post.setAuthorName(authorName);

        // 2. 生成唯一 slug
        post.setSlug(generateUniqueSlug(post.getTitle(), null));

        // 3. 设置默认状态及时间
        post.setStatus(Post.PostStatus.DRAFT);
        if (post.getCreatedAt() == null) {
            post.setCreatedAt(LocalDateTime.now());
        }
        post.setUpdatedAt(LocalDateTime.now());

        // 4. 初始化字段
        if (post.getStats() == null) post.setStats(new Post.PostStats());
        if (post.getComments() == null) post.setComments(new ArrayList<>());
        if (post.getTags() == null) post.setTags(new ArrayList<>());

        // 5. 保存博文
        return postRepository.save(post);
    }

    /**
     * 更新博文
     *
     * @param id 博文 ID
     * @param postUpdates 更新内容
     * @param currentUserId 当前操作用户 ID
     * @param isAdmin 是否为管理员
     * @return 更新后的博文
     * @throws RuntimeException 无权限或博文不存在
     */
    public Post updatePost(String id, Post postUpdates, Long currentUserId, boolean isAdmin) {
        return postRepository.findById(id)
                .map(existingPost -> {
                    // 1. 权限校验
                    if (!isAdmin && !existingPost.getAuthorId().equals(currentUserId)) {
                        throw new RuntimeException("Unauthorized: You are not the author of this post.");
                    }

                    // 2. 更新内容
                    boolean titleChanged = false;
                    if (postUpdates.getTitle() != null && !postUpdates.getTitle().equals(existingPost.getTitle())) {
                        existingPost.setTitle(postUpdates.getTitle());
                        titleChanged = true;
                    }
                    if (postUpdates.getContent() != null) existingPost.setContent(postUpdates.getContent());
                    if (postUpdates.getTags() != null) existingPost.setTags(postUpdates.getTags());
                    if (postUpdates.getStatus() != null) existingPost.setStatus(postUpdates.getStatus());
                    if (postUpdates.getFeaturedImage() != null) existingPost.setFeaturedImage(postUpdates.getFeaturedImage());

                    // 3. 更新 slug
                    if (titleChanged) {
                        existingPost.setSlug(generateUniqueSlug(existingPost.getTitle(), existingPost.getId()));
                    } else if (postUpdates.getSlug() != null && !postUpdates.getSlug().equals(existingPost.getSlug())) {
                        existingPost.setSlug(generateUniqueSlug(postUpdates.getSlug(), existingPost.getId()));
                    } else if (StringUtils.isEmpty(existingPost.getSlug())) {
                        existingPost.setSlug(generateUniqueSlug(existingPost.getTitle(), existingPost.getId()));
                    }

                    // 4. 设置更新时间
                    existingPost.setUpdatedAt(LocalDateTime.now());
                    return postRepository.save(existingPost);
                })
                .orElseThrow(() -> new RuntimeException("Post not found with id: " + id));
    }

    /**
     * 删除博文
     *
     * @param id 博文 ID
     * @param currentUserId 当前用户 ID
     * @param isAdmin 是否为管理员
     * @throws RuntimeException 无权限或博文不存在
     */
    public void deletePost(String id, Long currentUserId, boolean isAdmin) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found with id: " + id));

        if (!isAdmin && !post.getAuthorId().equals(currentUserId)) {
            throw new RuntimeException("Unauthorized: You are not the author of this post.");
        }

        postRepository.deleteById(id);
    }

    /**
     * 为博文添加评论
     *
     * @param postId 博文 ID
     * @param comment 评论内容（不包含作者信息）
     * @param authorId 作者 ID
     * @param authorName 作者名称
     * @return 添加评论后的博文实体
     */
    public Post addComment(String postId, Comment comment, Long authorId, String authorName) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found with id: " + postId));

        // 1. 设置评论信息
        comment.setId(UUID.randomUUID().toString());
        comment.setAuthorId(authorId);
        comment.setAuthorName(authorName);
        comment.setCreatedAt(LocalDateTime.now());

        // 2. 添加评论
        if (post.getComments() == null) {
            post.setComments(new ArrayList<>());
        }
        post.getComments().add(comment);

        return postRepository.save(post);
    }

    /**
     * 增加博文浏览量
     *
     * @param postId 博文 ID
     */
    public void incrementViews(String postId) {
        postRepository.findById(postId).ifPresent(post -> {
            post.getStats().setViews(post.getStats().getViews() + 1);
            postRepository.save(post);
        });
    }

    /**
     * 全文搜索博文
     *
     * @param keyword 搜索关键词
     * @return 匹配的博文列表
     */
    public List<Post> fullTextSearch(String keyword) {
        return postRepository.fullTextSearch(keyword);
    }

    /**
     * 根据标签推荐博文
     *
     * @param currentPostId 当前博文 ID
     * @param limit 推荐数量
     * @return 推荐博文列表
     */
    public List<Post> recommendPosts(String currentPostId, int limit) {
        Post currentPost = postRepository.findById(currentPostId)
                .orElseThrow(() -> new RuntimeException("Current post not found with id: " + currentPostId));

        List<String> tags = currentPost.getTags();
        if (tags == null || tags.isEmpty()) {
            return Collections.emptyList();
        }

        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "createdAt"));
        return postRepository.findByTagsInAndStatusOrderByCreatedAtDesc(tags, Post.PostStatus.PUBLISHED, pageable)
                .getContent();
    }

    /**
     * 删除博文中的评论
     *
     * @param postId 博文 ID
     * @param commentId 评论 ID
     * @param currentUserId 当前用户 ID
     * @param isAdmin 是否为管理员
     * @return 更新后的博文实体
     * @throws RuntimeException 评论不存在或无权限
     */
    public Post deleteComment(String postId, String commentId, Long currentUserId, boolean isAdmin) {
        return postRepository.findById(postId)
                .map(post -> {
                    if (post.getComments() == null) {
                        post.setComments(new ArrayList<>());
                    }

                    Comment comment = post.getComments().stream()
                            .filter(c -> c.getId().equals(commentId))
                            .findFirst()
                            .orElseThrow(() -> new RuntimeException("Comment not found with id: " + commentId));

                    if (!isAdmin && !comment.getAuthorId().equals(currentUserId)) {
                        throw new RuntimeException("Unauthorized: You are not the author of this comment.");
                    }

                    post.getComments().remove(comment);
                    return postRepository.save(post);
                })
                .orElseThrow(() -> new RuntimeException("Post not found with id: " + postId));
    }

    /**
     * 生成唯一的 slug
     *
     * @param title 标题或基础文本
     * @param excludeId 更新时排除的博文 ID（创建时为 null）
     * @return 唯一 slug 字符串
     */
    private String generateUniqueSlug(String title, String excludeId) {
        // 1. 清理标题并格式化
        String baseSlug = title == null ? "post" : title.toLowerCase()
                .replaceAll("[^a-zA-Z0-9\\u4e00-\\u9fa5\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .trim();

        if (baseSlug.isEmpty()) baseSlug = "untitled-post";
        if (baseSlug.startsWith("-")) baseSlug = baseSlug.substring(1);
        if (baseSlug.endsWith("-")) baseSlug = baseSlug.substring(0, baseSlug.length() - 1);
        if (baseSlug.isEmpty()) baseSlug = "untitled-post";

        // 2. 检查唯一性
        String uniqueSlug = baseSlug;
        int counter = 0;
        while (true) {
            Optional<Post> existing = postRepository.findBySlug(uniqueSlug);
            if (existing.isPresent() && (excludeId == null || !existing.get().getId().equals(excludeId))) {
                counter++;
                uniqueSlug = baseSlug + "-" + counter;
            } else {
                break;
            }
        }
        return uniqueSlug;
    }
}
