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
 * ClassName: PostRepository
 * Package: com.bryan.platform.repository
 * Description: 博文数据访问层，使用 Spring Data MongoDB。
 * Author: Bryan Long
 * Create: 2025/6/20
 * Version: v1.0
 */
@Repository // Mark as a Repository component
public interface PostRepository extends MongoRepository<Post, String> { // ID type is String

    // Find published posts, sorted by creation date descending (for pagination)
    Page<Post> findByStatusOrderByCreatedAtDesc(Post.PostStatus status, Pageable pageable);

    // --- MODIFICATION START ---

    // Previously: Page<Post> findByAuthorOrderByCreatedAtDesc(User author, Pageable pageable);
    // Reason for change: The 'Post' entity no longer has a 'User author' field.
    // Instead, it has 'Long authorId'.
    // Modified to: Find posts by authorId, sorted by creation date descending
    Page<Post> findByAuthorIdOrderByCreatedAtDesc(Long authorId, Pageable pageable);

    // Previously: Page<Post> findByAuthorAndStatus(User author, Post.PostStatus status, Pageable pageable);
    // Reason for change: The 'Post' entity no longer has a 'User author' field.
    // Modified to: Find posts by authorId and status
    Page<Post> findByAuthorIdAndStatus(Long authorId, Post.PostStatus status, Pageable pageable);

    // --- MODIFICATION END ---

    // Find post by slug
    Optional<Post> findBySlug(String slug);

    // Full-text search
    // Uses MongoDB's $text operator for full-text search
    // status: 'PUBLISHED' restricts search to only published posts
    @Query("{ '$text': { '$search': ?0 }, 'status': 'PUBLISHED' }")
    List<Post> fullTextSearch(String keyword);

    // Find published posts by a list of tags, sorted by creation date descending
    // Core change: Return type changed from List<Post> to Page<Post> to support getContent()
    Page<Post> findByTagsInAndStatusOrderByCreatedAtDesc(List<String> tags, Post.PostStatus status, Pageable pageable);

    /**
     * 根据一组ID查询博文列表，并分页排序。
     * 主要用于获取用户收藏的博文。
     *
     * @param ids      博文ID列表。
     * @param pageable 分页和排序信息。
     * @return 符合条件的博文分页列表。
     */
    @Query("{ '_id': { '$in': ?0 } }")
    Page<Post> findByIdIn(List<String> ids, Pageable pageable);
}