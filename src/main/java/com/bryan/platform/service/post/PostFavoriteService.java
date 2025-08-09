package com.bryan.platform.service.post;

import com.bryan.platform.exception.BusinessException;
import com.bryan.platform.exception.ResourceNotFoundException;
import com.bryan.platform.repository.PostFavoriteRepository;
import com.bryan.platform.repository.PostRepository;
import com.bryan.platform.domain.entity.post.Post;
import com.bryan.platform.domain.entity.post.PostFavorite;
import com.bryan.platform.service.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 博文收藏服务类
 * 处理用户对博文的收藏、取消收藏、查询收藏列表等功能
 *
 * @author Bryan Long
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
@RequiredArgsConstructor
public class PostFavoriteService {

    private final PostFavoriteRepository postFavoriteRepository;
    private final PostRepository postRepository;
    private final UserService userService;

    /**
     * 1. 先查 PostgreSQL 得到分页 ID 列表
     * 2. 再查 MongoDB 得到真正的 Post
     * 3. 手动组装 Page<Post>
     */
    public Page<Post> getFavoritePostsByUserId(Long userId, Pageable pageable) {
        userService.getUserById(userId);          // 不存在抛异常
        Page<PostFavorite> favoritePage = postFavoriteRepository.findByUserIdAndDeletedOrderByCreateTimeDesc(userId, 0, pageable);

        List<String> postIds = favoritePage.getContent()
                .stream()
                .map(PostFavorite::getPostId)
                .collect(Collectors.toList());

        if (postIds.isEmpty()) {
            return Page.empty(pageable);
        }

        Page<Post> postPage = postRepository.findByIdIn(postIds, pageable);
        // 保持总记录数、分页信息一致
        return new PageImpl<>(postPage.getContent(), pageable, favoritePage.getTotalElements());
    }

    /* 其余方法保持不变 */
    public boolean addFavorite(Long userId, String postId) {
        userService.getUserById(userId);
        if (!postRepository.existsById(postId)) {
            throw new ResourceNotFoundException("博文不存在");
        }
        if (postFavoriteRepository.findByUserIdAndPostIdAndDeleted(userId, postId, 0).isPresent()) {
            throw new BusinessException("已收藏");
        }
        PostFavorite fav = PostFavorite.builder()
                .userId(userId)
                .postId(postId)
                .deleted(0)
                .build();
        postFavoriteRepository.save(fav);
        return true;
    }

    public Boolean deleteFavorite(Long userId, String postId) {
        // 先查出未被逻辑删除的记录
        PostFavorite favorite = postFavoriteRepository
                .findByUserIdAndPostIdAndDeleted(userId, postId, 0)
                .orElseThrow(() -> new BusinessException("您尚未关注该用户"));

        // 触发 @SQLDelete 的 UPDATE
        postFavoriteRepository.delete(favorite);
        return true;
    }

    public boolean isFavorite(Long userId, String postId) {
        return postFavoriteRepository.findByUserIdAndPostIdAndDeleted(userId, postId, 0).isPresent();
    }

    public long countUserFavorites(Long userId) {
        return postFavoriteRepository.countByUserIdAndDeleted(userId, 0);
    }
}
