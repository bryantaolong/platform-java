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
 * ClassName: PostFavoriteServiceImpl
 * Package: com.bryan.platform.service.impl
 * Description: 博文收藏服务实现类。
 * 处理博文收藏的增删查业务逻辑。
 * Author: Bryan Long
 * Create: 2025/6/22 - 16:22
 * Version: v2.0
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
@RequiredArgsConstructor
public class PostFavoriteService {

    private final PostFavoriteMapper postFavoriteMapper;
    private final PostRepository postRepository;
    private final UserService userService;

    public Page<Post> getFavoritePostsByUserId(Long userId, Pageable pageable) {
        User user = userService.getUserById(userId);
        if (user == null) {
            throw new ResourceNotFoundException("用户未找到，ID: " + userId);
        }

        List<String> postIds = postFavoriteMapper.selectPostIdsByUserId(userId);
        if (postIds.isEmpty()) {
            return new PageImpl<>(Collections.emptyList(), pageable, 0);
        }

        return postRepository.findByIdIn(postIds, pageable);
    }

    public int addFavorite(Long userId, String postId) {
        User user = userService.getUserById(userId);
        if (user == null) {
            throw new ResourceNotFoundException("收藏用户未找到，ID: " + userId);
        }

        Optional<Post> postOptional = postRepository.findById(postId);
        if (postOptional.isEmpty()) {
            throw new ResourceNotFoundException("被收藏博文未找到，ID: " + postId);
        }

        PostFavorite existing = postFavoriteMapper.selectByUserIdAndPostId(userId, postId);
        if (existing != null) {
            if (existing.getDeleted() == 0) {
                throw new BusinessException("该博文已被收藏，无需重复收藏。");
            } else {
                existing.setDeleted(0);
                existing.setUpdateTime(LocalDateTime.now());
                return postFavoriteMapper.update(existing);
            }
        }

        PostFavorite favorite = PostFavorite.builder()
                .userId(userId)
                .postId(postId)
                .deleted(0)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();

        int rowsAffected = postFavoriteMapper.insert(favorite);
        if (rowsAffected > 0) {
            log.info("用户ID: {} 收藏博文ID: {}", userId, postId);
        }
        return rowsAffected;
    }

    public int deleteFavorite(Long userId, String postId) {
        PostFavorite existing = postFavoriteMapper.selectActiveByUserIdAndPostId(userId, postId);
        if (existing == null) {
            throw new ResourceNotFoundException("用户ID: " + userId + " 未收藏博文ID: " + postId + "。");
        }

        existing.setDeleted(1);
        existing.setUpdateTime(LocalDateTime.now());
        int rowsAffected = postFavoriteMapper.update(existing);
        if (rowsAffected > 0) {
            log.info("用户ID: {} 取消收藏博文ID: {}", userId, postId);
        }
        return rowsAffected;
    }

    public Boolean checkFavorite(Long userId, String postId) {
        PostFavorite favorite = postFavoriteMapper.selectActiveByUserIdAndPostId(userId, postId);
        return favorite != null;
    }
}
