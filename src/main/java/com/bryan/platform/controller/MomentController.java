package com.bryan.platform.controller;

import com.bryan.platform.common.constant.ErrorCode;
import com.bryan.platform.model.entity.Moment;
import com.bryan.platform.model.entity.User;
import com.bryan.platform.model.response.Result;
import com.bryan.platform.service.MomentService;
import com.bryan.platform.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 动态控制器 - 提供动态相关的RESTful接口，集成权限控制和数据校验
 *
 * @author Bryan Long
 * @version 2.0
 * @since 2025/7/15
 */
@RestController
@RequestMapping("/api/moments")
@RequiredArgsConstructor
public class MomentController {

    private final MomentService momentService;
    private final AuthService authService;

    /**
     * 创建动态（需登录）
     *
     * @param moment 动态内容（JSON格式）
     * @return 创建成功的动态数据
     * @throws IllegalArgumentException 如果参数无效
     */
    @PostMapping
    public Result<Moment> createMoment(@RequestBody Moment moment) {
        // 1. 获取当前用户信息
        User currentUser = authService.getCurrentUser();

        // 2. 设置作者信息
        moment.setAuthorId(currentUser.getId());
        moment.setAuthorName(currentUser.getUsername());

        // 3. 执行业务逻辑
        Moment savedMoment = momentService.save(moment);

        // 4. 返回成功响应
        return Result.success(savedMoment);
    }

    /**
     * 分页查询所有动态（公开接口）
     *
     * @param page 页码（从0开始，默认0）
     * @param size 每页数量（默认10）
     * @param sort 排序规则（格式：字段名,排序方向，默认createdAt,desc）
     * @return 分页结果
     */
    @GetMapping
    public Result<Page<Moment>> getAllMoments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort) {
        // 1. 解析排序参数
        String[] sortParams = sort.split(",");
        Sort.Direction direction = sortParams.length > 1 && "desc".equalsIgnoreCase(sortParams[1])
                ? Sort.Direction.DESC : Sort.Direction.ASC;

        // 2. 构建分页对象
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortParams[0]));

        // 3. 执行查询
        Page<Moment> moments = momentService.findAll(pageable);

        // 4. 返回结果
        return Result.success(moments);
    }

    /**
     * 获取动态详情（公开接口）
     *
     * @param id 动态ID
     * @return 动态详情数据
     * @throws IllegalArgumentException 如果ID无效
     */
    @GetMapping("/{id}")
    public Result<Moment> getMomentById(@PathVariable String id) {
        // 1. 查询动态
        return momentService.findById(id)
                .map(Result::success)
                .orElseGet(() -> Result.error(ErrorCode.NOT_FOUND));
    }

    /**
     * 获取用户动态列表（公开接口）
     *
     * @param userId 用户ID
     * @param page 页码（默认0）
     * @param size 每页数量（默认10）
     * @return 分页结果
     */
    @GetMapping("/user/{userId}")
    public Result<Page<Moment>> getUserMoments(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        // 1. 构建分页参数（强制按创建时间倒序）
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        // 2. 执行查询
        Page<Moment> moments = momentService.findByUserId(userId, pageable);

        // 3. 返回结果
        return Result.success(moments);
    }

    /**
     * 获取当前用户动态列表（需登录）
     *
     * @param page 页码（默认0）
     * @param size 每页数量（默认10）
     * @return 分页结果
     */
    @GetMapping("/me")
    public Result<Page<Moment>> getMyMoments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        // 1. 获取当前用户ID
        Long currentUserId = authService.getCurrentUserId();

        // 2. 构建分页参数
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        // 3. 执行查询
        return Result.success(momentService.findByUserId(currentUserId, pageable));
    }

    /**
     * 获取好友动态流（需登录）
     *
     * @param page 页码（默认0）
     * @param size 每页数量（默认10）
     * @return 分页结果
     */
    @GetMapping("/following")
    public Result<Page<Moment>> getFollowingMoments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        // 1. 获取当前用户ID
        Long currentUserId = authService.getCurrentUserId();

        // 2. 构建分页参数
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        // 3. 执行查询（好友列表在服务层获取）
        return Result.success(momentService.findFollowingMoments(currentUserId, pageable));
    }

    /**
     * 批量获取动态（公开接口）
     *
     * @param ids 动态ID列表
     * @return 动态列表
     * @throws IllegalArgumentException 如果ID列表为空
     */
    @PostMapping("/batch")
    public Result<List<Moment>> getMomentsByIds(@RequestBody List<String> ids) {
        // 1. 参数校验
        if (ids == null || ids.isEmpty()) {
            return Result.error(ErrorCode.BAD_REQUEST, "动态ID列表不能为空");
        }

        // 2. 执行查询
        List<Moment> moments = momentService.findByIds(ids);

        // 3. 返回结果
        return Result.success(moments);
    }

    /**
     * 删除动态（需作者或管理员权限）
     *
     * @param id 动态ID
     * @param userDetails 当前用户认证信息
     * @return 空响应体
     * @throws SecurityException 如果无操作权限
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteMoment(
            @PathVariable String id,
            @AuthenticationPrincipal UserDetails userDetails) {
        // 1. 获取当前用户权限信息
        Long currentUserId = authService.getCurrentUserId();
        boolean isAdmin = authService.isAdmin(userDetails);

        // 2. 执行删除（权限校验在服务层实现）
        momentService.deleteMoment(id, currentUserId, isAdmin);

        // 3. 返回成功响应
        return Result.success(null);
    }

    /**
     * 搜索动态内容（公开接口）
     *
     * @param keyword 搜索关键词
     * @return 匹配结果列表
     * @throws IllegalArgumentException 如果关键词为空
     */
    @GetMapping("/search")
    public Result<List<Moment>> searchMoments(@RequestParam String keyword) {
        // 1. 参数校验
        if (keyword == null || keyword.trim().isEmpty()) {
            return Result.error(ErrorCode.BAD_REQUEST, "搜索关键词不能为空");
        }

        // 2. 执行搜索
        List<Moment> moments = momentService.searchByContent(keyword);

        // 3. 返回结果
        return Result.success(moments);
    }

    /**
     * 获取热门动态（公开接口）
     *
     * @param minLikes 最小点赞数（默认100）
     * @param page 页码（默认0）
     * @param size 每页数量（默认10）
     * @return 分页结果
     */
    @GetMapping("/popular")
    public Result<Page<Moment>> getPopularMoments(
            @RequestParam(defaultValue = "100") int minLikes,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        // 1. 构建分页参数
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        // 2. 执行查询
        Page<Moment> moments = momentService.findPopularMoments(minLikes, pageable);

        // 3. 返回结果
        return Result.success(moments);
    }
}