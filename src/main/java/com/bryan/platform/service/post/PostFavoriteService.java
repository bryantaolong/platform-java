package com.bryan.platform.service.post;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.bryan.platform.common.exception.BusinessException;
import com.bryan.platform.common.exception.ResourceNotFoundException;
import com.bryan.platform.dao.mapper.PostFavoriteMapper;
import com.bryan.platform.dao.repository.PostRepository;
import com.bryan.platform.model.entity.post.Post;
import com.bryan.platform.model.entity.post.PostFavorite;
import com.bryan.platform.model.entity.user.User;
import com.bryan.platform.service.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 博文收藏服务类
 * 处理用户对博文的收藏、取消收藏、查询收藏列表等功能
 *
 * @author Bryan Long
 * @version 2.0
 * @since 2025/6/22
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
        User user = userService.getUserById(userId);
        if (user == null) {
            throw new ResourceNotFoundException("用户未找到，ID: " + userId);
        }

        // 查询用户收藏的博文ID列表
        LambdaQueryWrapper<PostFavorite> wrapper = new LambdaQueryWrapper<PostFavorite>()
                .eq(PostFavorite::getUserId, userId)
                .select(PostFavorite::getPostId);

        List<String> postIds = postFavoriteMapper.selectList(wrapper)
                .stream()
                .map(PostFavorite::getPostId)
                .collect(Collectors.toList());

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
        // 校验用户是否存在
        User user = userService.getUserById(userId);
        if (user == null) {
            throw new ResourceNotFoundException("收藏用户未找到，ID: " + userId);
        }

        // 校验博文是否存在
        Optional<Post> postOptional = postRepository.findById(postId);
        if (postOptional.isEmpty()) {
            throw new ResourceNotFoundException("被收藏博文未找到，ID: " + postId);
        }

        // 检查是否已收藏
        if (isFavorite(userId, postId)) {
            throw new BusinessException("该博文已被收藏，无需重复收藏");
        }

        // 插入新的收藏记录
        PostFavorite favorite = PostFavorite.builder()
                .userId(userId)
                .postId(postId)
                .build();

        int rowsAffected = postFavoriteMapper.insert(favorite);
        if (rowsAffected > 0) {
            log.info("用户ID: {} 收藏博文ID: {}", userId, postId);
            return true;
        }
        return false;
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
        // 直接删除收藏记录（物理删除）
        LambdaQueryWrapper<PostFavorite> wrapper = new LambdaQueryWrapper<PostFavorite>()
                .eq(PostFavorite::getUserId, userId)
                .eq(PostFavorite::getPostId, postId);

        int rowsAffected = postFavoriteMapper.delete(wrapper);

        if (rowsAffected > 0) {
            log.info("用户ID: {} 取消收藏博文ID: {}", userId, postId);
            return true;
        }
        throw new ResourceNotFoundException("用户ID: " + userId + " 未收藏博文ID: " + postId);
    }

    /**
     * 检查用户是否已收藏指定博文
     *
     * @param userId 用户 ID
     * @param postId 博文 ID
     * @return true 表示已收藏；false 表示未收藏
     */
    public boolean isFavorite(Long userId, String postId) {
        LambdaQueryWrapper<PostFavorite> wrapper = new LambdaQueryWrapper<PostFavorite>()
                .eq(PostFavorite::getUserId, userId)
                .eq(PostFavorite::getPostId, postId);
        return postFavoriteMapper.selectCount(wrapper) > 0;
    }

    public long countUserFavorites(Long userId) {
        LambdaQueryWrapper<PostFavorite> wrapper = new LambdaQueryWrapper<PostFavorite>()
                .eq(PostFavorite::getUserId, userId);
        return postFavoriteMapper.selectCount(wrapper);
    }
}
