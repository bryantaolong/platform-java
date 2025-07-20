package com.bryan.platform.controller;

import com.bryan.platform.model.response.Result;
import com.bryan.platform.common.enums.ErrorCode;
import com.bryan.platform.service.AuthService;
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
 * 控制器：博文收藏相关接口
 * 提供博文收藏的增删查操作，包括分页查询用户收藏、添加收藏、取消收藏和检查收藏状态。
 *
 * @author Bryan
 * @version 1.0
 * @since 2025/6/22
 */
@Slf4j
@RestController
@RequestMapping("/api/post_favorite")
@RequiredArgsConstructor
public class PostFavoriteController {

    private final PostFavoriteService postFavoriteService;
    private final AuthService authService;

    /**
     * 添加博文收藏。
     *
     * @param request 收藏请求体，包含博文ID
     * @return 操作结果信息
     */
    @PostMapping
    public Result<String> createFavorite(@Valid @RequestBody PostFavoriteAddRequest request) {
        // 1. 获取当前登录用户ID
        Long currentUserId = authService.getCurrentUserId();
        if (currentUserId == null) {
            log.warn("未认证用户尝试添加收藏。");
            return Result.error(ErrorCode.UNAUTHORIZED, "用户未登录或认证失败。");
        }

        // 2. 记录操作日志
        log.info("用户ID: {} 尝试收藏博文ID: {}", currentUserId, request.getPostId());

        // 3. 调用服务添加收藏
        postFavoriteService.addFavorite(currentUserId, request.getPostId());

        // 4. 返回成功提示
        return Result.success("收藏成功。");
    }

    /**
     * 分页获取指定用户的收藏博文列表。
     * <p>仅允许管理员或用户本人访问。</p>
     *
     * @param userId   目标用户ID
     * @param pageable 分页与排序参数，默认每页10条，按创建时间降序排序
     * @return 分页的收藏博文列表结果
     */
    @GetMapping("/{userId}")
    public Result<Page<Post>> getFavoritesByUserId(
            @PathVariable Long userId,
            @PageableDefault(size = 10, sort = {"createdAt"}, direction = Sort.Direction.DESC) Pageable pageable) {
        // 1. 获取当前登录用户ID，进行权限校验
        Long currentUserId = authService.getCurrentUserId();
        if (currentUserId == null) {
            log.warn("未认证用户尝试访问用户ID {} 的收藏列表。", userId);
            return Result.error(ErrorCode.UNAUTHORIZED, "用户未登录或认证失败。");
        }

        // 2. 记录日志
        log.info("用户ID: {} 请求获取用户ID: {} 的收藏列表，分页信息: {}", currentUserId, userId, pageable);

        // 3. 调用服务获取收藏列表
        Page<Post> favoritePosts = postFavoriteService.getFavoritePostsByUserId(userId, pageable);

        // 4. 返回成功结果
        return Result.success(favoritePosts);
    }

    /**
     * 取消收藏指定博文。
     * <p>仅允许管理员或收藏者本人操作。</p>
     *
     * @param postId 目标博文ID
     * @return 操作结果信息
     */
    @DeleteMapping("/post/{postId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public Result<String> deleteFavorite(@PathVariable String postId) {
        // 1. 获取当前登录用户ID
        Long currentUserId = authService.getCurrentUserId();
        if (currentUserId == null) {
            log.warn("未认证用户尝试取消收藏博文ID: {}。", postId);
            return Result.error(ErrorCode.UNAUTHORIZED, "用户未登录或认证失败。");
        }

        // 2. 检查收藏状态及权限
        if (!postFavoriteService.checkFavorite(currentUserId, postId)) {
            return Result.error(ErrorCode.UNAUTHORIZED, "无权操作该收藏。");
        }

        // 3. 记录日志
        log.info("用户ID: {} 取消收藏博文ID: {}", currentUserId, postId);

        // 4. 执行取消收藏操作
        postFavoriteService.deleteFavorite(currentUserId, postId);

        // 5. 返回操作结果
        return Result.success("对博文 '" + postId + "' 的收藏已取消。");
    }

    /**
     * 检查当前用户是否收藏指定博文。
     *
     * @param postId 博文ID
     * @return true表示已收藏，false表示未收藏
     */
    @GetMapping("/check/{postId}")
    public Result<Boolean> checkFavorite(@PathVariable String postId) {
        // 1. 记录日志
        log.info("检查用户是否收藏博文ID: {}", postId);

        // 2. 获取当前用户ID
        Long currentUserId = authService.getCurrentUserId();
        if (currentUserId == null) {
            return Result.error(ErrorCode.UNAUTHORIZED, "用户未登录或认证失败。");
        }

        // 3. 调用服务检查收藏状态
        Boolean isFavorite = postFavoriteService.checkFavorite(currentUserId, postId);

        // 4. 返回结果
        return Result.success(isFavorite);
    }
}
