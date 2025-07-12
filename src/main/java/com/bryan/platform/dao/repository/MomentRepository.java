package com.bryan.platform.dao.repository;

import com.bryan.platform.model.entity.Moment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Moment 动态数据访问层
 *
 * @author Bryan Long
 * @version 1.0
 * @since 2025/7/11
 */
@Repository
public interface MomentRepository extends MongoRepository<Moment, String> {

    // 1. 基础查询方法

    // 按作者ID分页查询动态（最新优先）
    Page<Moment> findByAuthorIdOrderByCreatedAtDesc(Long authorId, Pageable pageable);

    // 批量查询指定作者ID列表的动态（用于好友动态流）
    Page<Moment> findByAuthorIdInOrderByCreatedAtDesc(List<Long> authorIds, Pageable pageable);

    // 2. 互动相关查询

    // 查询点赞数大于指定值的动态（热门动态）
    Page<Moment> findByLikeCountGreaterThanOrderByCreatedAtDesc(Integer minLikeCount, Pageable pageable);

    // 3. 全文搜索（如果配置了全文索引）
    @Query("{ '$text': { '$search': ?0 } }")
    List<Moment> searchByContent(String keyword);

    // 4. 复杂条件查询

    // 查询包含图片的动态
    @Query("{ 'images': { '$exists': true, '$ne': null, '$ne': '' } }")
    Page<Moment> findWithImages(Pageable pageable);

    // 5. 时间范围查询

    // 查询指定时间范围内的动态
    @Query("{ 'created_at': { '$gte': ?0, '$lte': ?1 } }")
    List<Moment> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    // 6. 评论相关查询

    // 查询有评论的动态
    @Query("{ 'comments': { '$exists': true, '$not': { '$size': 0 } } }")
    Page<Moment> findWithComments(Pageable pageable);

    // 7. 批量操作

    // 批量查询动态（用于消息推送等场景）
    @Query("{ '_id': { '$in': ?0 } }")
    List<Moment> findByIds(List<String> ids);

    // 8. 统计相关

    // 统计用户动态数量
    long countByAuthorId(Long authorId);
}