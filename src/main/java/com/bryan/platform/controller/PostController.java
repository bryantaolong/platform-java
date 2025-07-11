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
 * 控制器：博文管理接口
 * 提供博文的增删改查、评论管理、全文搜索与推荐功能，集成 MongoDB 存储与 Spring Security 安全控制。
 *
 * @author Bryan
 * @version v1.2
 * @since 2025/6/20
 */
@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;
    private final AuthService authService;

    /**
     * 管理员获取所有博文（支持分页与排序）
     *
     * @param page    页码
     * @param size    每页大小
     * @param sortBy  排序字段
     * @param sortDir 排序方向
     * @return 所有博文分页数据
     */
    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Page<Post>> getAllPostsForAdmin(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(sortDir), sortBy));
        return Result.success(postService.getAllPosts(pageable));
    }


    /**
     * 获取所有已发布博文（支持分页与排序）。
     *
     * @param page    页码，默认 0
     * @param size    每页大小，默认 10
     * @param sortBy  排序字段，默认 createdAt
     * @param sortDir 排序方向，默认 DESC
     * @return 分页博文列表
     */
    @GetMapping
    public Result<Page<Post>> getAllPublishedPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir) {
        // 1. 构造分页对象
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(sortDir), sortBy));
        // 2. 获取已发布博文列表
        return Result.success(postService.getPublishedPosts(pageable));
    }

    /**
     * 根据博文 ID 获取详情。
     *
     * @param id 博文 ID
     * @return 博文详情
     */
    @GetMapping("/{id}")
    public Result<Post> getPostById(@PathVariable String id) {
        // 1. 查询博文
        return Result.success(postService.getPostById(id));
    }

    /**
     * 根据 Slug 获取博文，并自动增加浏览量。
     *
     * @param slug 博文 Slug
     * @return 博文详情
     */
    @GetMapping("/slug/{slug}")
    public Result<Post> getPostBySlug(@PathVariable String slug) {
        // 1. 查询博文
        Post post = postService.getPostBySlug(slug);
        // 2. 增加浏览量
        postService.incrementViews(post.getId());
        // 3. 返回结果
        return Result.success(post);
    }

    /**
     * 获取指定作者的博文列表（分页）。
     *
     * @param authorId 作者用户 ID
     * @param page     页码
     * @param size     每页大小
     * @param sortBy   排序字段
     * @param sortDir  排序方向
     * @return 作者发布的博文分页数据
     */
    @GetMapping("/author/{authorId}")
    public Result<Page<Post>> getPostsByAuthorId(
            @PathVariable Long authorId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir) {
        // 1. 构造分页参数
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(sortDir), sortBy));
        // 2. 获取作者博文
        return Result.success(postService.getPostsByAuthorId(authorId, pageable));
    }

    /**
     * 获取当前登录用户的博文列表。
     *
     * @param page    页码
     * @param size    每页大小
     * @param sortBy  排序字段
     * @param sortDir 排序方向
     * @return 当前用户发布的博文分页数据
     */
    @GetMapping("/me")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public Result<Page<Post>> getMyPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir) {
        // 1. 获取当前用户 ID
        Long currentUserId = authService.getCurrentUserId();
        // 2. 构造分页参数
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(sortDir), sortBy));
        // 3. 查询博文
        return Result.success(postService.getPostsByAuthorId(currentUserId, pageable));
    }

    /**
     * 获取关注用户发布的博文。
     *
     * @param page    页码
     * @param size    每页大小
     * @param sortBy  排序字段
     * @param sortDir 排序方向
     * @return 被关注用户的博文分页数据
     */
    @GetMapping("/following")
    public Result<Page<Post>> getFollowingPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir) {
        // 1. 获取当前用户 ID
        Long currentUserId = authService.getCurrentUserId();
        // 2. 构造分页对象
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(sortDir), sortBy));
        // 3. 获取关注博文
        Page<Post> posts = postService.getFollowingPosts(currentUserId, pageable);
        return Result.success(posts);
    }

    /**
     * 创建新博文。
     *
     * @param post 博文数据
     * @return 创建后的博文
     */
    @PostMapping("/post")
    public Result<Post> createPost(@RequestBody Post post) {
        // 1. 获取当前用户信息
        User currentUser = authService.getCurrentUser();
        // 2. 创建博文
        return Result.success(postService.createPost(post, currentUser.getId(), currentUser.getUsername()));
    }

    /**
     * 更新博文。
     *
     * @param id          博文 ID
     * @param postUpdates 博文更新内容
     * @param userDetails 当前用户信息
     * @return 更新后的博文
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public Result<Post> updatePost(
            @PathVariable String id,
            @RequestBody Post postUpdates,
            @AuthenticationPrincipal UserDetails userDetails) {
        // 1. 获取当前用户 ID 和权限
        Long currentUserId = authService.getCurrentUserId();
        boolean isAdmin = authService.isAdmin(userDetails);
        // 2. 执行更新逻辑（权限控制在服务层处理）
        return Result.success(postService.updatePost(id, postUpdates, currentUserId, isAdmin));
    }

    /**
     * 删除博文。
     *
     * @param id          博文 ID
     * @param userDetails 当前用户信息
     * @return 操作结果（无内容）
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public Result<Void> deletePost(
            @PathVariable String id,
            @AuthenticationPrincipal UserDetails userDetails) {
        // 1. 获取当前用户 ID 和角色
        Long currentUserId = authService.getCurrentUserId();
        boolean isAdmin = authService.isAdmin(userDetails);
        // 2. 执行删除操作
        postService.deletePost(id, currentUserId, isAdmin);
        return Result.success(null);
    }

    /**
     * 添加评论到指定博文。
     *
     * @param postId  博文 ID
     * @param comment 评论内容
     * @return 更新后的博文（含新评论）
     */
    @PostMapping("/{postId}/comments")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public Result<Post> addComment(
            @PathVariable String postId,
            @RequestBody Comment comment) {
        // 1. 获取当前用户信息
        User currentUser = authService.getCurrentUser();
        Long authorId = currentUser.getId();
        String authorName = currentUser.getUsername();
        // 2. 添加评论
        return Result.success(postService.addComment(postId, comment, authorId, authorName));
    }

    /**
     * 删除指定博文中的评论。
     *
     * @param postId    博文 ID
     * @param commentId 评论 ID
     * @param userDetails 当前用户信息
     * @return 更新后的博文
     */
    @DeleteMapping("/{postId}/comments/{commentId}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public Result<Post> deleteComment(
            @PathVariable String postId,
            @PathVariable String commentId,
            @AuthenticationPrincipal UserDetails userDetails) {
        // 1. 获取当前用户 ID 和角色
        Long currentUserId = authService.getCurrentUserId();
        boolean isAdmin = authService.isAdmin(userDetails);
        // 2. 删除评论
        Post updatedPost = postService.deleteComment(postId, commentId, currentUserId, isAdmin);
        return Result.success(updatedPost);
    }

    /**
     * 博文全文搜索。
     *
     * @param query 搜索关键词
     * @return 匹配结果列表
     */
    @GetMapping("/search")
    public Result<List<Post>> searchPosts(@RequestParam String query) {
        // 1. 执行全文搜索
        return Result.success(postService.fullTextSearch(query));
    }

    /**
     * 推荐博文。
     *
     * @param currentPostId 当前博文 ID
     * @param limit 推荐数量限制
     * @return 推荐结果列表
     */
    @GetMapping("/recommendations/{currentPostId}")
    public Result<List<Post>> getRecommendations(
            @PathVariable String currentPostId,
            @RequestParam(defaultValue = "5") int limit) {
        // 1. 获取推荐博文
        List<Post> recommendedPosts = postService.recommendPosts(currentPostId, limit);
        return Result.success(recommendedPosts);
    }
}
