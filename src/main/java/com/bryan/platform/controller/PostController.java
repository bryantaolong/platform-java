package com.bryan.platform.controller;

import com.bryan.platform.model.response.Result;
import com.bryan.platform.model.entity.Comment;
import com.bryan.platform.model.entity.Post;
import com.bryan.platform.model.entity.User;
import com.bryan.platform.service.AuthService;
import com.bryan.platform.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * ClassName: PostController
 * Package: com.bryan.platform.controller
 * Description: 博文相关的 RESTful API 控制器。
 * 适配 MongoDB 作为数据存储，并集成 Spring Security 进行认证和授权管理。
 * Author: Bryan Long
 * Create: 2025/6/20
 * Version: v1.2 - Redundancy Removed
 */
@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;
    private final AuthService authService; // Used to retrieve current user details

    /**
     * 获取所有已发布的博文列表（支持分页和排序）。
     * Accessible by anyone.
     *
     * @param page    页码 (默认为 0，表示第一页)。
     * @param size    每页大小 (默认为 10)。
     * @param sortBy  排序字段 (默认为 "createdAt"，博文创建时间)。
     * @param sortDir 排序方向 (默认为 "DESC"，降序)。
     * @return 包含分页博文数据的 Result。
     */
    @GetMapping
    public Result<Page<Post>> getAllPublishedPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(sortDir), sortBy));
        return Result.success(postService.getPublishedPosts(pageable));
    }

    /**
     * 根据博文 ID 获取详情。
     * 此接口对所有用户开放（包括未登录用户）。
     *
     * @param id 博文 ID (MongoDB 的 String 类型 ID)
     * @return 博文实体
     */
    @GetMapping("/{id}")
    public Result<Post> getPostById(@PathVariable String id) {
        return Result.success(postService.getPostById(id));
    }

    /**
     * 根据 Slug 获取单篇博文详情，并自动增加该博文的浏览量。
     * Accessible by anyone.
     *
     * @param slug 博文的唯一标识符（URL 友好）。
     * @return 博文实体。
     */
    @GetMapping("/slug/{slug}")
    public Result<Post> getPostBySlug(@PathVariable String slug) {
        Post post = postService.getPostBySlug(slug);
        postService.incrementViews(post.getId());
        return Result.success(post);
    }

    /**
     * 获取指定用户发布的所有博文（分页）。
     * 支持公开访问（例如个人主页浏览），无需登录。
     *
     * @param authorId 用户的 authorId（MySQL 中的 Long 类型 ID）
     * @param page     页码，默认 0
     * @param size     每页大小，默认 10
     * @param sortBy   排序字段，默认 createdAt
     * @param sortDir  排序方向，默认 DESC
     * @return 指定用户发布的博文分页列表
     */
    @GetMapping("/author/{authorId}")
    public Result<Page<Post>> getPostsByAuthorId(
            @PathVariable Long authorId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(sortDir), sortBy));
        return Result.success(postService.getPostsByAuthorId(authorId, pageable));
    }

    /**
     * 获取当前认证用户发布的所有博文（分页）。
     * Requires 'USER' or 'ADMIN' role.
     *
     * @param page    页码，默认 0
     * @param size    每页大小，默认 10
     * @param sortBy  排序字段，默认 createdAt
     * @param sortDir 排序方向，默认 DESC
     * @return 当前用户发布的博文分页列表
     */
    @GetMapping("/me")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public Result<Page<Post>> getMyPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir) {
        Long currentUserId = authService.getCurrentUserId();
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(sortDir), sortBy));
        return Result.success(postService.getPostsByAuthorId(currentUserId, pageable));
    }

    /**
     * 获取当前认证用户关注的人发布的博文。
     * Accessible by authenticated users.
     *
     * @param page    页码，默认 0
     * @param size    每页大小，默认 10
     * @param sortBy  排序字段，默认 createdAt
     * @param sortDir 排序方向，默认 DESC
     * @return 关注用户发布的博文分页列表
     */
    @GetMapping("/following")
    public Result<Page<Post>> getFollowingPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir) {
        Long currentUserId = authService.getCurrentUserId();
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(sortDir), sortBy));
        Page<Post> posts = postService.getFollowingPosts(currentUserId, pageable);
        return Result.success(posts);
    }

    /**
     * 创建新博文。
     * Only users with 'USER' or 'ADMIN' role can create posts.
     *
     * @param post 博文数据，通过请求体传入。
     * @return 创建后的博文实体。
     */
    @PostMapping
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public Result<Post> createPost(@RequestBody Post post) {
        User currentUser = authService.getCurrentUser();
        return Result.success(postService.createPost(post, currentUser.getId(), currentUser.getUsername()));
    }

    /**
     * 更新博文。
     * Only 'ADMIN' role or the post author can perform this operation.
     * Authorization logic (owner or admin) is handled within the service layer.
     *
     * @param id          博文ID (MongoDB 的 String 类型 ID)。
     * @param postUpdates 包含更新内容的博文实体，通过请求体传入。
     * @return 更新后的博文实体。
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public Result<Post> updatePost(
            @PathVariable String id,
            @RequestBody Post postUpdates,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long currentUserId = authService.getCurrentUserId();
        boolean isAdmin = authService.isAdmin(userDetails);
        // The service layer should handle the authorization check (is current user owner or admin?)
        return Result.success(postService.updatePost(id, postUpdates, currentUserId, isAdmin));
    }

    /**
     * 删除博文。
     * Only 'ADMIN' role or the post author can perform this operation.
     * Authorization logic (owner or admin) is handled within the service layer.
     *
     * @param id          博文ID (MongoDB 的 String 类型 ID)。
     * @return Result indicating success.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public Result<Void> deletePost(
            @PathVariable String id,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long currentUserId = authService.getCurrentUserId();
        boolean isAdmin = authService.isAdmin(userDetails);

        postService.deletePost(id, currentUserId, isAdmin);
        // Correctly return success after deletion
        return Result.success(null);
    }

    /**
     * 为指定博文添加评论。
     * Only users with 'USER' or 'ADMIN' role can add comments.
     *
     * @param postId      博文ID (MongoDB 的 String 类型 ID)。
     * @param comment     评论内容，通过请求体传入。
     * @return 添加评论后的完整博文实体（因为评论内嵌在博文文档中）。
     */
    @PostMapping("/{postId}/comments")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public Result<Post> addComment(
            @PathVariable String postId,
            @RequestBody Comment comment) {
        User currentUser = authService.getCurrentUser();
        Long authorId = currentUser.getId();
        String authorName = currentUser.getUsername();

        return Result.success(postService.addComment(postId, comment, authorId, authorName));
    }

    /**
     * 删除博文中的评论。
     * Only 'ADMIN' role or the comment author can perform this operation.
     * Authorization logic (owner or admin) is handled within the service layer.
     *
     * @param postId      博文ID (MongoDB 的 String 类型 ID)。
     * @param commentId   评论ID (MongoDB 内嵌评论的 String 类型 ID)。
     * @return 更新后的博文实体（评论已被移除）。
     */
    @DeleteMapping("/{postId}/comments/{commentId}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public Result<Post> deleteComment(
            @PathVariable String postId,
            @PathVariable String commentId,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long currentUserId = authService.getCurrentUserId();
        boolean isAdmin = authService.isAdmin(userDetails);

        Post updatedPost = postService.deleteComment(postId, commentId, currentUserId, isAdmin);
        return Result.success(updatedPost);
    }

    /**
     * 对已发布的博文执行全文搜索。
     * Accessible by anyone.
     *
     * @param query 搜索关键词。
     * @return 匹配关键词的博文列表。
     */
    @GetMapping("/search")
    public Result<List<Post>> searchPosts(@RequestParam String query) {
        return Result.success(postService.fullTextSearch(query));
    }

    /**
     * 推荐博文。
     * 根据当前博文的标签，推荐其他相关的已发布博文。
     * Accessible by anyone.
     *
     * @param currentPostId 当前博文ID (MongoDB 的 String 类型 ID)。
     * @param limit         推荐数量限制 (默认为 5)。
     * @return 推荐的博文列表。
     */
    @GetMapping("/recommendations/{currentPostId}")
    public Result<List<Post>> getRecommendations(
            @PathVariable String currentPostId,
            @RequestParam(defaultValue = "5") int limit) {
        List<Post> recommendedPosts = postService.recommendPosts(currentPostId, limit);
        return Result.success(recommendedPosts);
    }
}