package com.bryan.platform.service.moment;

import com.bryan.platform.dao.repository.MomentRepository;
import com.bryan.platform.model.entity.moment.Moment;
import com.bryan.platform.model.entity.user.User;
import com.bryan.platform.service.user.UserFollowService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 动态服务层 - 处理动态相关的业务逻辑
 *
 * @author Bryan Long
 * @version 1.0
 * @since 2025/7/12
 */
@Service
@RequiredArgsConstructor
public class MomentService {

    private final MomentRepository momentRepository;
    private final UserFollowService userFollowService;

    /**
     * 保存动态信息
     *
     * @param moment 动态实体对象
     * @return 保存后的动态对象
     */
    public Moment save(Moment moment) {
        // 1. 参数校验（简单示例，实际项目应更完善）
        if (moment == null) {
            throw new IllegalArgumentException("动态对象不能为空");
        }

        // 2. 执行保存操作
        return momentRepository.save(moment);
    }

    /**
     * 根据ID查询动态
     *
     * @param id 动态ID
     * @return 包含动态的Optional对象
     * @throws IllegalArgumentException 如果ID为空
     */
    public Optional<Moment> findById(String id) {
        // 1. 参数校验
        if (id == null || id.isEmpty()) {
            throw new IllegalArgumentException("动态ID不能为空");
        }

        // 2. 执行查询操作
        return momentRepository.findById(id);
    }

    /**
     * 分页查询所有动态
     *
     * @param pageable 分页参数
     * @return 动态分页数据
     */
    public Page<Moment> findAll(Pageable pageable) {
        // 1. 执行全量分页查询
        return momentRepository.findAll(pageable);
    }

    /**
     * 根据ID删除动态
     *
     * @param id 动态ID
     * @throws IllegalArgumentException 如果ID为空
     */
    public void deleteMoment(String id, Long userId, Boolean isAdmin) {
        // 1. 参数校验
        if (id == null || id.isEmpty()) {
            throw new IllegalArgumentException("动态ID不能为空");
        }

        Moment moment = momentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Moment not found with id: " + id));

        if (!isAdmin && !moment.getAuthorId().equals(userId)) {
            throw new RuntimeException("Unauthorized: You are not the author of this moment.");
        }

        // 2. 执行删除操作
        momentRepository.deleteById(id);
    }

    /**
     * 根据用户ID分页查询动态（按创建时间倒序）
     *
     * @param authorId 用户ID
     * @param pageable 分页参数
     * @return 用户动态分页数据
     * @throws IllegalArgumentException 如果用户ID为空
     */
    public Page<Moment> findByUserId(Long authorId, Pageable pageable) {
        // 1. 参数校验
        if (authorId == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }

        // 2. 执行用户动态查询
        return momentRepository.findByAuthorIdOrderByCreatedAtDesc(authorId, pageable);
    }

    public Page<Moment> findFollowingMoments(Long userId, Pageable pageable) {
        // 1. 获取关注的用户列表
        var followingUsers = userFollowService.getFollowingUsers(
                userId,
                pageable.getPageNumber() + 1,
                pageable.getPageSize()
        );

        // 2. 若无关注用户，返回空分页
        if (followingUsers.getRecords().isEmpty()) {
            return new PageImpl<>(Collections.emptyList(), pageable, 0);
        }

        // 3. 获取被关注用户的 ID 列表
        List<Long> followingIds = followingUsers.getRecords()
                .stream()
                .map(User::getId)
                .collect(Collectors.toList());

        // 4. 查询对应用户的已发布博文
        return momentRepository.findByAuthorIdInOrderByCreatedAtDesc(
                followingIds,
                pageable
        );
    }

    /**
     * 统计用户动态数量
     *
     * @param authorId 用户ID
     * @return 动态数量
     * @throws IllegalArgumentException 如果用户ID为空
     */
    public long countByUserId(Long authorId) {
        // 1. 参数校验
        if (authorId == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }

        // 2. 执行统计操作
        return momentRepository.countByAuthorId(authorId);
    }

    /**
     * 删除用户所有动态
     *
     * @param authorId 用户ID
     * @return 删除的动态数量
     * @throws IllegalArgumentException 如果用户ID为空
     */
    public int deleteByAuthorId(Long authorId) {
        // 1. 参数校验
        if (authorId == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }

        // 2. 查询用户所有动态（不分页）
        List<Moment> moments = momentRepository
                .findByAuthorIdOrderByCreatedAtDesc(authorId, Pageable.unpaged())
                .getContent();

        // 3. 批量删除动态
        momentRepository.deleteAll(moments);

        // 4. 返回删除数量
        return moments.size();
    }

    /**
     * 查询好友动态流（按创建时间倒序）
     *
     * @param authorIds 好友ID列表
     * @param pageable 分页参数
     * @return 好友动态分页数据
     * @throws IllegalArgumentException 如果好友ID列表为空
     */
    public Page<Moment> findFriendMoments(List<Long> authorIds, Pageable pageable) {
        // 1. 参数校验
        if (authorIds == null || authorIds.isEmpty()) {
            throw new IllegalArgumentException("好友ID列表不能为空");
        }

        // 2. 执行好友动态查询
        return momentRepository.findByAuthorIdInOrderByCreatedAtDesc(authorIds, pageable);
    }

    /**
     * 根据关键词搜索动态内容
     *
     * @param keyword 搜索关键词
     * @return 匹配的动态列表
     * @throws IllegalArgumentException 如果关键词为空
     */
    public List<Moment> searchByContent(String keyword) {
        // 1. 参数校验
        if (keyword == null || keyword.trim().isEmpty()) {
            throw new IllegalArgumentException("搜索关键词不能为空");
        }

        // 2. 执行全文搜索
        return momentRepository.searchByContent(keyword);
    }

    /**
     * 查询包含图片的动态
     *
     * @param pageable 分页参数
     * @return 含图片的动态分页数据
     */
    public Page<Moment> findMomentsWithImages(Pageable pageable) {
        // 1. 执行带图片动态查询
        return momentRepository.findWithImages(pageable);
    }

    /**
     * 查询指定时间范围内的动态
     *
     * @param start 开始时间
     * @param end 结束时间
     * @return 时间范围内的动态列表
     * @throws IllegalArgumentException 如果时间参数无效
     */
    public List<Moment> findBetweenDates(LocalDateTime start, LocalDateTime end) {
        // 1. 参数校验
        if (start == null || end == null) {
            throw new IllegalArgumentException("时间参数不能为空");
        }
        if (start.isAfter(end)) {
            throw new IllegalArgumentException("开始时间不能晚于结束时间");
        }

        // 2. 执行时间范围查询
        return momentRepository.findByCreatedAtBetween(start, end);
    }

    /**
     * 查询热门动态（点赞数超过阈值）
     *
     * @param minLikeCount 最小点赞数阈值
     * @param pageable 分页参数
     * @return 热门动态分页数据
     * @throws IllegalArgumentException 如果阈值小于0
     */
    public Page<Moment> findPopularMoments(int minLikeCount, Pageable pageable) {
        // 1. 参数校验
        if (minLikeCount < 0) {
            throw new IllegalArgumentException("点赞数阈值不能为负数");
        }

        // 2. 执行热门动态查询
        return momentRepository.findByLikeCountGreaterThanOrderByCreatedAtDesc(minLikeCount, pageable);
    }

    /**
     * 查询有评论的动态
     *
     * @param pageable 分页参数
     * @return 含评论的动态分页数据
     */
    public Page<Moment> findMomentsWithComments(Pageable pageable) {
        // 1. 执行带评论动态查询
        return momentRepository.findWithComments(pageable);
    }

    /**
     * 批量查询动态
     *
     * @param ids 动态ID列表
     * @return 动态列表
     * @throws IllegalArgumentException 如果ID列表为空
     */
    public List<Moment> findByIds(List<String> ids) {
        // 1. 参数校验
        if (ids == null || ids.isEmpty()) {
            throw new IllegalArgumentException("动态ID列表不能为空");
        }

        // 2. 执行批量查询
        return momentRepository.findByIds(ids);
    }
}