package com.bryan.platform.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bryan.platform.common.enums.ErrorCode;
import com.bryan.platform.service.AuthService;
import com.bryan.platform.model.response.Result;
import com.bryan.platform.model.entity.User;
import com.bryan.platform.service.UserFollowService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 用户关注关系控制器
 * <p>
 * 提供用户关注与取消关注、查询关注列表、粉丝列表及关注状态检查的 RESTful API。
 * 依赖 AuthService 获取当前用户信息，UserFollowService 处理关注业务逻辑。
 * </p>
 *
 * @author Bryan
 * @version 1.0
 * @since 2025/6/23
 */
@RestController
@RequestMapping("/api/user_follow")
@RequiredArgsConstructor
public class UserFollowController {

    private final UserFollowService userFollowService;
    private final AuthService authService;

    /**
     * 当前用户关注指定用户。
     *
     * @param followingId 被关注用户ID
     * @return 关注操作是否成功，true表示成功
     */
    @PostMapping("/follow/{followingId}")
    public Result<Boolean> followUser(
            @PathVariable Long followingId) {
        try {
            // 1. 获取当前登录用户ID
            Long currentUserId = authService.getCurrentUserId();
            // 2. 调用关注服务执行关注操作
            return Result.success(userFollowService.followUser(currentUserId, followingId));
        } catch (RuntimeException e) {
            // 3. 异常捕获，返回错误响应
            return Result.error(ErrorCode.BAD_REQUEST, e.getMessage());
        }
    }

    /**
     * 当前用户取消关注指定用户。
     *
     * @param followingId 被取消关注用户ID
     * @return 取消关注是否成功，true表示成功
     */
    @PostMapping("/unfollow/{followingId}")
    public Result<Boolean> unfollowUser(
            @PathVariable Long followingId) {
        try {
            // 1. 获取当前登录用户实体及ID
            User currentUser = authService.getCurrentUser();
            Long currentUserId = currentUser.getId();
            // 2. 调用关注服务执行取消关注操作
            return Result.success(userFollowService.unfollowUser(currentUserId, followingId));
        } catch (RuntimeException e) {
            // 3. 异常捕获，返回错误响应
            return Result.error(ErrorCode.BAD_REQUEST, e.getMessage());
        }
    }

    /**
     * 查询指定用户关注的用户列表（分页）。
     *
     * @param userId   用户ID
     * @param pageNum  页码，默认 1
     * @param pageSize 每页大小，默认 10
     * @return 分页的关注用户列表
     */
    @GetMapping("/following/{userId}")
    public Result<Page<User>> getFollowingUsers(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        try {
            // 1. 调用服务获取关注用户分页列表
            return Result.success(userFollowService.getFollowingUsers(userId, pageNum, pageSize));
        } catch (RuntimeException e) {
            // 2. 异常捕获，返回错误信息
            return Result.error(ErrorCode.BAD_REQUEST, e.getMessage());
        }
    }

    /**
     * 查询指定用户的粉丝列表（分页）。
     *
     * @param userId   用户ID
     * @param pageNum  页码，默认 1
     * @param pageSize 每页大小，默认 10
     * @return 分页的粉丝用户列表
     */
    @GetMapping("/followers/{userId}")
    public Result<Page<User>> getFollowerUsers(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        try {
            // 1. 调用服务获取粉丝用户分页列表
            return Result.success(userFollowService.getFollowerUsers(userId, pageNum, pageSize));
        } catch (RuntimeException e) {
            // 2. 异常捕获，返回错误信息
            return Result.error(ErrorCode.BAD_REQUEST, e.getMessage());
        }
    }

    /**
     * 检查当前用户是否关注指定用户。
     *
     * @param followingId 被检查的目标用户ID
     * @return true表示已关注，false表示未关注
     */
    @GetMapping("/check/{followingId}")
    public Result<Boolean> isFollowing(
            @PathVariable Long followingId) {
        try {
            // 1. 获取当前登录用户ID
            Long currentUserId = authService.getCurrentUserId();
            // 2. 调用服务判断关注状态
            return Result.success(userFollowService.isFollowing(currentUserId, followingId));
        } catch (RuntimeException e) {
            // 3. 异常捕获，返回错误信息
            return Result.error(ErrorCode.BAD_REQUEST, e.getMessage());
        }
    }
}
