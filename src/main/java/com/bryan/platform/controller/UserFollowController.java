package com.bryan.platform.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bryan.platform.common.constant.ErrorCode;
import com.bryan.platform.common.util.JwtUtil;
import com.bryan.platform.model.response.Result;
import com.bryan.platform.model.entity.User;
import com.bryan.platform.service.UserFollowService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * ClassName: UserFollowController
 * Package: com.bryan.platform.controller
 * Description:
 * Author: Bryan Long
 * Create: 2025/6/23 - 18:57
 * Version: v1.0
 */
@RestController
@RequestMapping("/api/user_follow")
@RequiredArgsConstructor
public class UserFollowController {

    private final UserFollowService userFollowService;

    @PostMapping("/follow/{followingId}")
    @PreAuthorize("hasRole('USER')")
    public Result<Boolean> followUser(
            @PathVariable Long followingId) {
        try {
            Long currentUserId = JwtUtil.getCurrentUserId();
            return Result.success(userFollowService.followUser(currentUserId, followingId));
        } catch (RuntimeException e) {
            return Result.error(ErrorCode.BAD_REQUEST, e.getMessage());
        }
    }

    @PostMapping("/unfollow/{followingId}")
    @PreAuthorize("hasRole('USER')")
    public Result<Boolean> unfollowUser(
            @PathVariable Long followingId) {
        try {
            Long currentUserId = JwtUtil.getCurrentUserId();
            return Result.success(userFollowService.unfollowUser(currentUserId, followingId));
        } catch (RuntimeException e) {
            return Result.error(ErrorCode.BAD_REQUEST, e.getMessage());
        }
    }

    @GetMapping("/following/{userId}")
    public Result<Page<User>> getFollowingUsers(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        try {
            return Result.success(userFollowService.getFollowingUsers(userId, pageNum, pageSize));
        } catch (RuntimeException e) {
            return Result.error(ErrorCode.BAD_REQUEST, e.getMessage());
        }
    }

    @GetMapping("/followers/{userId}")
    public Result<Page<User>> getFollowerUsers(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        try {
            return Result.success(userFollowService.getFollowerUsers(userId, pageNum, pageSize));
        } catch (RuntimeException e) {
            return Result.error(ErrorCode.BAD_REQUEST, e.getMessage());
        }
    }

    @GetMapping("/check/{followingId}")
    public Result<Boolean> isFollowing(
            @PathVariable Long followingId) {
        try {
            Long currentUserId = JwtUtil.getCurrentUserId();
            return Result.success(userFollowService.isFollowing(currentUserId, followingId));
        } catch (RuntimeException e) {
            return Result.error(ErrorCode.BAD_REQUEST, e.getMessage());
        }
    }
}
