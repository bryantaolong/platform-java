package com.bryan.platform.dao.repository;

import com.bryan.platform.model.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 博文数据访问层，使用Spring Data MongoDB实现
 *
 * @author Bryan Long
 * @version 1.0
 * @since 2025/6/20
 */
@Repository // 标记为Repository组件
public interface PostRepository extends MongoRepository<Post, String> { // ID类型为String

    // 查询已发布的博文，按创建时间降序排列（用于分页）
    Page<Post> findByStatusOrderByCreatedAtDesc(Post.PostStatus status, Pageable pageable);

    // --- 修改开始 ---

    // 原方法：Page<Post> findByAuthorOrderByCreatedAtDesc(User author, Pageable pageable);
    // 修改原因：Post实体不再包含User author字段
    // 改为：根据authorId查询博文，按创建时间降序排列
    Page<Post> findByAuthorIdOrderByCreatedAtDesc(Long authorId, Pageable pageable);

    // 原方法：Page<Post> findByAuthorAndStatus(User author, Post.PostStatus status, Pageable pageable);
    // 修改原因：Post实体不再包含User author字段
    // 改为：根据authorId和状态查询博文
    Page<Post> findByAuthorIdAndStatus(Long authorId, Post.PostStatus status, Pageable pageable);

    // 根据多个作者ID和状态查询博文，按创建时间降序排列
    Page<Post> findByAuthorIdInAndStatusOrderByCreatedAtDesc(
            List<Long> authorIds,
            Post.PostStatus status,
            Pageable pageable
    );

    // --- 修改结束 ---

    // 根据slug查询博文
    Optional<Post> findBySlug(String slug);

    // 全文搜索
    // 使用MongoDB的$text操作符进行全文搜索
    // status: 'PUBLISHED'限制只搜索已发布的博文
    @Query("{ '$text': { '$search': ?0 }, 'status': 'PUBLISHED' }")
    List<Post> fullTextSearch(String keyword);

    // 根据标签列表查询已发布的博文，按创建时间降序排列
    // 核心变更：返回类型从List<Post>改为Page<Post>以支持分页内容获取
    Page<Post> findByTagsInAndStatusOrderByCreatedAtDesc(List<String> tags, Post.PostStatus status, Pageable pageable);

    /**
     * 根据一组ID查询博文列表，并分页排序
     * 主要用于获取用户收藏的博文
     *
     * @param ids      博文ID列表
     * @param pageable 分页和排序信息
     * @return 符合条件的博文分页列表
     */
    @Query("{ '_id': { '$in': ?0 } }")
    Page<Post> findByIdIn(List<String> ids, Pageable pageable);
}