package com.bryan.platform.controller;

import com.bryan.platform.domain.entity.user.SysUser;
import com.bryan.platform.domain.response.PageResult;
import com.bryan.platform.service.user.AuthService;
import com.bryan.platform.domain.response.Result;
import com.bryan.platform.service.user.UserFollowService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 用户关注关系控制器
 * <p>
 * 提供用户关注与取消关注、查询关注列表、粉丝列表及关注状态检查的 RESTful API。
 * 依赖 AuthService 获取当前用户信息，UserFollowService 处理关注业务逻辑。
 * </p>
 *
 * @author Bryan Long
 */
@RestController
@RequestMapping("/api/user-follows")
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
    public Result<Boolean> followUser(@PathVariable Long followingId) {
        Long currentUserId = authService.getCurrentUserId();
        return Result.success(userFollowService.followUser(currentUserId, followingId) > 0);
    }

    /**
     * 当前用户取消关注指定用户。
     *
     * @param followingId 被取消关注用户ID
     * @return 取消关注是否成功，true表示成功
     */
    @PostMapping("/unfollow/{followingId}")
    public Result<Boolean> unfollowUser(@PathVariable Long followingId) {
        Long currentUserId = authService.getCurrentUserId();
        return Result.success(userFollowService.unfollowUser(currentUserId, followingId) > 0);
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
    public Result<PageResult<SysUser>> getFollowingUsers(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(userFollowService.getFollowingUsers(userId, pageNum, pageSize));
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
    public Result<PageResult<SysUser>> getFollowerUsers(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(userFollowService.getFollowerUsers(userId, pageNum, pageSize));
    }

    /**
     * 检查当前用户是否关注指定用户。
     *
     * @param followingId 被检查的目标用户ID
     * @return true表示已关注，false表示未关注
     */
    @GetMapping("/check/{followingId}")
    public Result<Boolean> isFollowing(@PathVariable Long followingId) {
        Long currentUserId = authService.getCurrentUserId();
        return Result.success(userFollowService.isFollowing(currentUserId, followingId));
    }
}
