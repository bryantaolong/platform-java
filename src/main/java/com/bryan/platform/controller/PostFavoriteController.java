package com.bryan.platform.controller;

import com.bryan.platform.model.response.Result; // Changed from ApiResult to your Result class
import com.bryan.platform.common.constant.ErrorCode; // Import your ErrorCode enum
import com.bryan.platform.util.JwtUtil; // 用于获取当前用户ID
import com.bryan.platform.model.request.PostFavoriteAddRequest;
import com.bryan.platform.model.entity.Post; // 用于收藏列表的返回类型
import com.bryan.platform.service.PostFavoriteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault; // 用于设置分页默认值
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize; // Spring Security 权限注解
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid; // 用于JSR 303/349 Bean Validation


/**
 * ClassName: PostFavoriteController
 * Package: com.bryan.platform.controller
 * Description: 博文收藏相关API接口。
 * 处理博文收藏的增删查接口。
 * Author: Bryan Long
 * Create: 2025/6/22 - 16:22
 * Version: v1.0
 */
@Slf4j
@RestController
@RequestMapping("/api/post_favorite")
@RequiredArgsConstructor // 使用 Lombok 自动生成构造函数进行依赖注入
public class PostFavoriteController {

    private final PostFavoriteService postFavoriteService;

    /**
     * 获取当前用户收藏的博文列表（分页）。
     * URL: GET /api/post_favorite/my?page=0&size=10&sort=createdAt,desc
     *
     * @param pageable 分页参数 (page, size, sort)。默认按创建时间倒序。
     * @return 收藏博文的分页数据
     */
    @GetMapping("/my")
    // 只有认证用户才能访问
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Result<Page<Post>>> getMyFavorites(
            @PageableDefault(size = 10, sort = {"createdAt"}, direction = Sort.Direction.DESC) Pageable pageable) {

        Long currentUserId = JwtUtil.getCurrentUserId();
        // 理论上 isAuthenticated() 会确保 currentUserId 不为空，但为了健壮性可以再判断
        if (currentUserId == null) {
            log.warn("未认证用户尝试访问收藏列表。");
            // Corrected: Use ErrorCode.UNAUTHORIZED
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Result.error(ErrorCode.UNAUTHORIZED, "用户未登录或认证失败。"));
        }

        log.info("获取用户ID: {} 的收藏列表, 分页信息: {}", currentUserId, pageable);
        Page<Post> favoritePosts = postFavoriteService.getFavoritePostsByUserId(currentUserId, pageable);
        // Using Result.success to wrap the paginated data
        return ResponseEntity.ok(Result.success(favoritePosts));
    }

    /**
     * 添加博文到当前用户的收藏列表。
     * URL: POST /api/post_favorite
     * 请求体: { "postId": "要收藏的MongoDB博文ID" }
     *
     * @param request 包含要收藏的博文ID的请求体
     * @return 成功信息
     */
    @PostMapping
    @PreAuthorize("isAuthenticated()") // 只有认证用户才能添加收藏
    public ResponseEntity<Result<String>> addFavorite(@Valid @RequestBody PostFavoriteAddRequest request) {
        Long currentUserId = JwtUtil.getCurrentUserId();
        if (currentUserId == null) {
            log.warn("未认证用户尝试添加收藏。");
            // Corrected: Use ErrorCode.UNAUTHORIZED
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Result.error(ErrorCode.UNAUTHORIZED, "用户未登录或认证失败。"));
        }

        log.info("用户ID: {} 尝试收藏博文ID: {}", currentUserId, request.getPostId());
        postFavoriteService.addFavorite(currentUserId, request.getPostId());
        // Using Result.success for success message
        return ResponseEntity.status(HttpStatus.CREATED).body(Result.success("收藏成功。"));
    }

    /**
     * 根据收藏记录的唯一ID取消收藏（逻辑删除）。
     * URL: DELETE /api/post_favorite/{favoriteId}
     *
     * @param favoriteId 收藏记录的唯一ID (MySQL 中 PostFavorite 表的ID)
     * @return 成功信息
     */
    @DeleteMapping("/{favoriteId}")
    // 只有认证用户，并且是该收藏记录的拥有者或管理员才能删除
    @PreAuthorize("isAuthenticated() and (hasRole('ADMIN') or @postFavoriteService.checkFavoriteOwnership(#favoriteId, authentication.principal.id))")
    public ResponseEntity<Result<String>> deleteFavoriteById(@PathVariable String favoriteId) {
        // @PreAuthorize 已经处理了用户身份和所有权检查
        log.info("尝试删除收藏记录ID: {}", favoriteId);
        postFavoriteService.deleteFavoriteById(favoriteId);
        // Using Result.success for success message
        return ResponseEntity.ok(Result.success("收藏记录已取消。"));
    }

    /**
     * 根据博文ID取消当前用户对该博文的收藏（逻辑删除）。
     * URL: DELETE /api/post_favorite/post/{postId}
     *
     * @param postId 被取消收藏的博文ID (MongoDB 中的 Post 表ID)
     * @return 成功信息
     */
    @DeleteMapping("/post/{postId}")
    @PreAuthorize("isAuthenticated()") // 只有认证用户才能取消自己的收藏
    public ResponseEntity<Result<String>> deleteFavoriteByPostId(@PathVariable String postId) {
        Long currentUserId = JwtUtil.getCurrentUserId();
        if (currentUserId == null) {
            log.warn("未认证用户尝试根据博文ID取消收藏。");
            // Corrected: Use ErrorCode.UNAUTHORIZED
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Result.error(ErrorCode.UNAUTHORIZED, "用户未登录或认证失败。"));
        }

        log.info("用户ID: {} 尝试取消博文ID: {} 的收藏", currentUserId, postId);
        postFavoriteService.deleteFavoriteByUserIdAndPostId(currentUserId, postId);
        // Using Result.success for success message
        return ResponseEntity.ok(Result.success("对博文 '" + postId + "' 的收藏已取消。"));
    }

    /**
     * 检查当前用户是否已收藏指定博文。
     * URL: GET /api/post_favorite/check/{postId}
     *
     * @param postId 要检查的博文ID (MongoDB ID)
     * @return 如果已收藏返回 true，否则返回 false
     */
    @GetMapping("/check/{postId}")
    // 允许未认证用户访问，因为服务层会处理用户ID为空的情况，返回false
    public ResponseEntity<Result<Boolean>> checkFavorite(@PathVariable String postId) {
        log.info("检查用户是否收藏博文ID: {}", postId);
        Long currentUserId = JwtUtil.getCurrentUserId();
        if (currentUserId == null) {
            return ResponseEntity.notFound().build();
        }

        Boolean isFavorite = postFavoriteService.checkFavorite(currentUserId, postId);
        // Using Result.success to wrap the boolean result
        return ResponseEntity.ok(Result.success(isFavorite));
    }
}