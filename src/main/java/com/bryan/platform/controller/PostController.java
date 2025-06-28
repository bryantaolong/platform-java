package com.bryan.platform.controller;

import com.bryan.platform.model.response.Result;
import com.bryan.platform.model.entity.Comment; // 需要 Comment 实体
import com.bryan.platform.model.entity.Post;
import com.bryan.platform.model.entity.User; // 需要 User 实体来获取作者信息
import com.bryan.platform.service.AuthService;
import com.bryan.platform.service.PostService;
import com.bryan.platform.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
 * Version: v1.0
 */
@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    private final AuthService authService;

    /**
     * 获取所有已发布的博文列表（支持分页和排序）。
     *
     * @param page    页码 (默认为 0，表示第一页)。
     * @param size    每页大小 (默认为 10)。
     * @param sortBy  排序字段 (默认为 "createdAt"，博文创建时间)。
     * @param sortDir 排序方向 (默认为 "DESC"，降序)。
     * @return 包含分页博文数据的 ResponseEntity。
     */
    @GetMapping
    public ResponseEntity<Result<Page<Post>>> getAllPublishedPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir) {
        // 根据传入的排序方向和字段构建 Sort 对象
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        // 创建 Pageable 对象，包含分页和排序信息
        Pageable pageable = PageRequest.of(page, size, sort);
        // 调用服务层方法获取分页博文
        return ResponseEntity.ok(Result.success(postService.getPublishedPosts(pageable)));
    }

    /**
     * 根据 Slug 获取单篇博文详情，并自动增加该博文的浏览量。
     *
     * @param slug 博文的唯一标识符（URL 友好）。
     * @return 博文实体。
     */
    @GetMapping("/{slug}")
    public ResponseEntity<Result<Post>> getPostBySlug(@PathVariable String slug) {
        Post post = postService.getPostBySlug(slug);
        postService.incrementViews(post.getId()); // 增加浏览量，MongoDB 的 ID 是 String 类型
        return ResponseEntity.ok(Result.success(post));
    }

    /**
     * 创建新博文。
     * 只有拥有 'USER' 角色认证。
     *
     * @param post        博文数据，通过请求体传入。
     * @return 创建后的博文实体。
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED) // HTTP 状态码 201 Created
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Result<Post>> createPost(
            @RequestBody Post post) {
        // 从 Spring Security 的 UserDetails 中获取用户名，并通过 UserService 加载完整的用户实体
        User currentUser = authService.getCurrentUser();
        Long authorId = currentUser.getId(); // 获取用户ID (MySQL 中的 Long 类型 ID)
        String authorName = currentUser.getUsername(); // 获取用户名作为作者名称

        return ResponseEntity.ok(Result.success(postService.createPost(post, authorId, authorName)));
    }

    /**
     * 更新博文。
     * 只有拥有 'ADMIN' 角色或博文作者才能执行此操作。
     *
     * @param id          博文ID (MongoDB 的 String 类型 ID)。
     * @param postUpdates 包含更新内容的博文实体，通过请求体传入。
     * @param userDetails 当前认证用户的 Spring Security UserDetails 对象。
     * @return 更新后的博文实体。
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<Result<Post>> updatePost(
            @PathVariable String id, // 从 URL 路径中获取博文 ID
            @RequestBody Post postUpdates,
            @AuthenticationPrincipal UserDetails userDetails) {

        User currentUser = authService.getCurrentUser();
        Long currentUserId = currentUser.getId();
        boolean isAdmin = userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")); // 检查是否为管理员

        return ResponseEntity.ok(Result.success(postService.updatePost(id, postUpdates, currentUserId, isAdmin)));
    }

    /**
     * 删除博文。
     * 只有拥有 'ADMIN' 角色或博文作者才能执行此操作。
     *
     * @param id 博文ID (MongoDB 的 String 类型 ID)。
     * @param userDetails 当前认证用户的 Spring Security UserDetails 对象。
     * @return 无内容响应 (HTTP 204 No Content)。
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Result<Void>> deletePost(
            @PathVariable String id, // 从 URL 路径中获取博文 ID
            @AuthenticationPrincipal UserDetails userDetails) {

        User currentUser = (User) authService.loadUserByUsername(userDetails.getUsername());
        Long currentUserId = currentUser.getId();
        boolean isAdmin = userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        postService.deletePost(id, currentUserId, isAdmin);
        return ResponseEntity.noContent().build(); // 返回 204 No Content 状态码
    }

    /**
     * 为指定博文添加评论。
     * 只有拥有 'USER' 角色认证。
     *
     * @param postId      博文ID (MongoDB 的 String 类型 ID)。
     * @param comment     评论内容，通过请求体传入。
     * @param userDetails 当前认证用户的 Spring Security UserDetails 对象。
     * @return 添加评论后的完整博文实体（因为评论内嵌在博文文档中）。
     */
    @PostMapping("/{postId}/comments")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Result<Post>> addComment(
            @PathVariable String postId, // 从 URL 路径中获取博文 ID
            @RequestBody Comment comment,
            @AuthenticationPrincipal UserDetails userDetails) {
        // 从 Spring Security 的 UserDetails 中获取用户名，并通过 UserService 加载完整的用户实体
        User currentUser = (User) authService.loadUserByUsername(userDetails.getUsername());
        Long authorId = currentUser.getId(); // 获取评论作者的用户ID (MySQL 中的 Long 类型 ID)
        String authorName = currentUser.getUsername(); // 获取评论作者的用户名

        return ResponseEntity.ok(Result.success(postService.addComment(postId, comment, authorId, authorName)));
    }

    /**
     * 删除博文中的评论。
     * 只有拥有 'ADMIN' 角色或评论作者才能执行此操作。
     *
     * @param postId      博文ID (MongoDB 的 String 类型 ID)。
     * @param commentId   评论ID (MongoDB 内嵌评论的 String 类型 ID)。
     * @param userDetails 当前认证用户的 Spring Security UserDetails 对象。
     * @return 更新后的博文实体（评论已被移除）。
     */
    @DeleteMapping("/{postId}/comments/{commentId}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Result<Post>> deleteComment(
            @PathVariable String postId, // 从 URL 路径中获取博文 ID
            @PathVariable String commentId, // 从 URL 路径中获取评论 ID
            @AuthenticationPrincipal UserDetails userDetails) {

        User currentUser = (User) authService.loadUserByUsername(userDetails.getUsername());
        Long currentUserId = currentUser.getId();
        boolean isAdmin = userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        Post updatedPost = postService.deleteComment(postId, commentId, currentUserId, isAdmin);
        return ResponseEntity.ok(Result.success(updatedPost));
    }


    /**
     * 对已发布的博文执行全文搜索。
     * 此接口无需认证，对所有用户开放。
     *
     * @param query 搜索关键词。
     * @return 匹配关键词的博文列表。
     */
    @GetMapping("/search")
    public ResponseEntity<Result<List<Post>>> searchPosts(@RequestParam String query) {
        return ResponseEntity.ok(Result.success(postService.fullTextSearch(query)));
    }

    /**
     * 推荐博文。
     * 根据当前博文的标签，推荐其他相关的已发布博文。
     * 此接口无需认证，对所有用户开放。
     *
     * @param currentPostId 当前博文ID (MongoDB 的 String 类型 ID)。
     * @param limit         推荐数量限制 (默认为 5)。
     * @return 推荐的博文列表。
     */
    @GetMapping("/recommendations/{currentPostId}")
    public ResponseEntity<Result<List<Post>>> getRecommendations(
            @PathVariable String currentPostId, // 从 URL 路径中获取当前博文 ID
            @RequestParam(defaultValue = "5") int limit) {
        List<Post> recommendedPosts = postService.recommendPosts(currentPostId, limit);
        return ResponseEntity.ok(Result.success(recommendedPosts));
    }
}