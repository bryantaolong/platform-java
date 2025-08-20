package com.bryan.platform.service.post;

import com.bryan.platform.domain.entity.user.SysUser;
import com.bryan.platform.exception.BusinessException;
import com.bryan.platform.exception.ResourceNotFoundException;
import com.bryan.platform.mapper.PostFavoriteMapper;
import com.bryan.platform.repository.PostRepository;
import com.bryan.platform.domain.entity.post.Post;
import com.bryan.platform.domain.entity.post.PostFavorite;
import com.bryan.platform.service.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

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

    private final PostFavoriteMapper postFavoriteMapper;
    private final PostRepository postRepository;
    private final UserService userService;

    /**
     * 获取指定用户收藏的博文分页列表
     *
     * @param userId 用户 ID
     * @param pageable 分页参数
     * @return 用户收藏的博文分页结果
     * @throws ResourceNotFoundException 如果用户不存在
     */
    public Page<Post> getFavoritePostsByUserId(Long userId, Pageable pageable) {
        // 校验用户是否存在
        SysUser user = userService.getUserById(userId);
        if (user == null) {
            throw new ResourceNotFoundException("用户未找到，ID: " + userId);
        }

        // 查询用户收藏的博文ID列表
        List<String> postIds = postFavoriteMapper.selectPostIdsByUserId(userId);

        if (postIds.isEmpty()) {
            return new PageImpl<>(Collections.emptyList(), pageable, 0);
        }

        // 根据博文ID查询博文内容并分页返回
        return postRepository.findByIdIn(postIds, pageable);
    }

    /**
     * 添加博文收藏记录
     *
     * @param userId 用户 ID
     * @param postId 被收藏的博文 ID
     * @return 影响的数据库行数
     * @throws ResourceNotFoundException 如果用户或博文不存在
     * @throws BusinessException 如果已收藏且未删除
     */
    public boolean addFavorite(Long userId, String postId) {
        userService.getUserById(userId);

        // 校验博文是否存在
        if (!postRepository.existsById(postId)) {
            throw new ResourceNotFoundException("博文不存在，ID: " + postId);
        }

        if (postFavoriteMapper.countByUserIdAndPostId(userId, postId) > 0) {
            throw new BusinessException("该博文已被收藏，无需重复收藏");
        }

        int rows = postFavoriteMapper.insert(
                PostFavorite.builder()
                        .userId(userId)
                        .postId(postId)
                        .createdAt(LocalDateTime.now())
                        .build());
        return rows > 0;
    }

    /**
     * 取消用户对博文的收藏
     *
     * @param userId 用户 ID
     * @param postId 被取消收藏的博文 ID
     * @return 影响的数据库行数
     * @throws ResourceNotFoundException 如果未收藏该博文
     */
    public boolean deleteFavorite(Long userId, String postId) {
        int rows = postFavoriteMapper.deleteByUserIdAndPostId(userId, postId);
        if (rows == 0) {
            throw new ResourceNotFoundException("用户未收藏该博文");
        }
        return true;
    }

    /* ------------------ 工具方法 ------------------ */
    /**
     * 检查用户是否已收藏指定博文
     *
     * @param userId 用户 ID
     * @param postId 博文 ID
     * @return true 表示已收藏；false 表示未收藏
     */
    public boolean isFavorite(Long userId, String postId) {
        return postFavoriteMapper.countByUserIdAndPostId(userId, postId) > 0;
    }

    public long countUserFavorites(Long userId) {
        return postFavoriteMapper.countByUserId(userId);
    }
}
