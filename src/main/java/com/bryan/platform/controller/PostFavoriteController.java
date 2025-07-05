package com.bryan.platform.controller;

import com.bryan.platform.model.response.Result;
import com.bryan.platform.common.constant.ErrorCode;
import com.bryan.platform.util.JwtUtil;
import com.bryan.platform.model.request.PostFavoriteAddRequest;
import com.bryan.platform.model.entity.Post;
import com.bryan.platform.service.PostFavoriteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

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
@RequiredArgsConstructor
public class PostFavoriteController {

    private final PostFavoriteService postFavoriteService;

    @GetMapping("/my")
    public Result<Page<Post>> getMyFavorites(
            @PageableDefault(size = 10, sort = {"createdAt"}, direction = Sort.Direction.DESC) Pageable pageable) {

        Long currentUserId = JwtUtil.getCurrentUserId();
        if (currentUserId == null) {
            log.warn("未认证用户尝试访问收藏列表。");
            return Result.error(ErrorCode.UNAUTHORIZED, "用户未登录或认证失败。");
        }

        log.info("获取用户ID: {} 的收藏列表, 分页信息: {}", currentUserId, pageable);
        Page<Post> favoritePosts = postFavoriteService.getFavoritePostsByUserId(currentUserId, pageable);
        return Result.success(favoritePosts);
    }

    @PostMapping
    public Result<String> addFavorite(@Valid @RequestBody PostFavoriteAddRequest request) {
        Long currentUserId = JwtUtil.getCurrentUserId();
        if (currentUserId == null) {
            log.warn("未认证用户尝试添加收藏。");
            return Result.error(ErrorCode.UNAUTHORIZED, "用户未登录或认证失败。");
        }

        log.info("用户ID: {} 尝试收藏博文ID: {}", currentUserId, request.getPostId());
        postFavoriteService.addFavorite(currentUserId, request.getPostId());
        return Result.success("收藏成功。");
    }

    @DeleteMapping("/post/{postId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public Result<String> deleteFavorite(@PathVariable String postId) {
        Long currentUserId = JwtUtil.getCurrentUserId();
        if (currentUserId == null) {
            log.warn("未认证用户尝试根据博文ID取消收藏。");
            return Result.error(ErrorCode.UNAUTHORIZED, "用户未登录或认证失败。");
        }

        if(!postFavoriteService.checkFavorite(currentUserId, postId)) {
            return Result.error(ErrorCode.UNAUTHORIZED);
        }

        log.info("用户ID: {} 尝试取消博文ID: {} 的收藏", currentUserId, postId);
        postFavoriteService.deleteFavorite(currentUserId, postId);
        return Result.success("对博文 '" + postId + "' 的收藏已取消。");
    }

    @GetMapping("/check/{postId}")
    public Result<Boolean> checkFavorite(@PathVariable String postId) {
        log.info("检查用户是否收藏博文ID: {}", postId);
        Long currentUserId = JwtUtil.getCurrentUserId();
        if (currentUserId == null) {
            return Result.error(ErrorCode.UNAUTHORIZED);
        }

        Boolean isFavorite = postFavoriteService.checkFavorite(currentUserId, postId);
        return Result.success(isFavorite);
    }
}