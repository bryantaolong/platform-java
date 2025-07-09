package com.bryan.platform.service;

import com.bryan.platform.common.exception.BusinessException;
import com.bryan.platform.common.exception.ResourceNotFoundException;
import com.bryan.platform.dao.mapper.PostFavoriteMapper;
import com.bryan.platform.dao.repository.PostRepository;
import com.bryan.platform.model.entity.Post;
import com.bryan.platform.model.entity.PostFavorite;
import com.bryan.platform.model.entity.User;
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
import java.util.Optional;

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
        // 1. 校验用户是否存在
        User user = userService.getUserById(userId);
        if (user == null) {
            throw new ResourceNotFoundException("用户未找到，ID: " + userId);
        }

        // 2. 查询用户收藏的博文 ID 列表
        List<String> postIds = postFavoriteMapper.selectPostIdsByUserId(userId);
        if (postIds.isEmpty()) {
            return new PageImpl<>(Collections.emptyList(), pageable, 0);
        }

        // 3. 根据博文 ID 查询博文内容并分页返回
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
    public int addFavorite(Long userId, String postId) {
        // 1. 校验用户是否存在
        User user = userService.getUserById(userId);
        if (user == null) {
            throw new ResourceNotFoundException("收藏用户未找到，ID: " + userId);
        }

        // 2. 校验博文是否存在
        Optional<Post> postOptional = postRepository.findById(postId);
        if (postOptional.isEmpty()) {
            throw new ResourceNotFoundException("被收藏博文未找到，ID: " + postId);
        }

        // 3. 判断是否已收藏过该博文
        PostFavorite existing = postFavoriteMapper.selectByUserIdAndPostId(userId, postId);
        if (existing != null) {
            if (existing.getDeleted() == 0) {
                // 3.1 已收藏且未删除，不允许重复收藏
                throw new BusinessException("该博文已被收藏，无需重复收藏。");
            } else {
                // 3.2 已收藏但已逻辑删除，恢复收藏状态
                existing.setDeleted(0);
                existing.setUpdateTime(LocalDateTime.now());
                return postFavoriteMapper.update(existing);
            }
        }

        // 4. 插入新的收藏记录
        PostFavorite favorite = PostFavorite.builder()
                .userId(userId)
                .postId(postId)
                .deleted(0)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();

        int rowsAffected = postFavoriteMapper.insert(favorite);

        // 5. 记录日志
        if (rowsAffected > 0) {
            log.info("用户ID: {} 收藏博文ID: {}", userId, postId);
        }

        return rowsAffected;
    }

    /**
     * 取消用户对博文的收藏
     *
     * @param userId 用户 ID
     * @param postId 被取消收藏的博文 ID
     * @return 影响的数据库行数
     * @throws ResourceNotFoundException 如果未收藏该博文
     */
    public int deleteFavorite(Long userId, String postId) {
        // 1. 查询是否存在有效的收藏记录
        PostFavorite existing = postFavoriteMapper.selectActiveByUserIdAndPostId(userId, postId);
        if (existing == null) {
            throw new ResourceNotFoundException("用户ID: " + userId + " 未收藏博文ID: " + postId + "。");
        }

        // 2. 逻辑删除收藏记录
        existing.setDeleted(1);
        existing.setUpdateTime(LocalDateTime.now());
        int rowsAffected = postFavoriteMapper.update(existing);

        // 3. 记录日志
        if (rowsAffected > 0) {
            log.info("用户ID: {} 取消收藏博文ID: {}", userId, postId);
        }

        return rowsAffected;
    }

    /**
     * 检查用户是否已收藏指定博文
     *
     * @param userId 用户 ID
     * @param postId 博文 ID
     * @return true 表示已收藏；false 表示未收藏
     */
    public Boolean checkFavorite(Long userId, String postId) {
        // 1. 查询是否存在未删除的收藏记录
        PostFavorite favorite = postFavoriteMapper.selectActiveByUserIdAndPostId(userId, postId);
        return favorite != null;
    }
}
