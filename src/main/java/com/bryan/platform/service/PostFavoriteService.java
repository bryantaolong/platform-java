package com.bryan.platform.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
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

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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

        QueryWrapper<PostFavorite> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId.toString());
        queryWrapper.eq("deleted", 0);
        queryWrapper.select("post_id");

        List<PostFavorite> favoriteEntities = postFavoriteMapper.selectList(queryWrapper);
        List<String> favoritePostIds = favoriteEntities.stream()
                .map(PostFavorite::getPostId)
                .collect(Collectors.toList());

        if (favoritePostIds.isEmpty()) {
            return new PageImpl<>(Collections.emptyList(), pageable, 0);
        }

        return postRepository.findByIdIn(favoritePostIds, pageable);
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

        QueryWrapper<PostFavorite> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId.toString());
        queryWrapper.eq("post_id", postId);
        queryWrapper.eq("deleted", 0);
        Long existingFavoriteCount = postFavoriteMapper.selectCount(queryWrapper);

        if (existingFavoriteCount > 0) {
            throw new BusinessException("该博文已被收藏，无需重复收藏。");
        }

        PostFavorite favorite = PostFavorite.builder()
                .userId(userId.toString())
                .postId(postId)
                .deleted(0)
                .build();

        int rowsAffected = postFavoriteMapper.insert(favorite);
        if (rowsAffected > 0) {
            log.info("用户ID: {} 收藏博文ID: {}", userId, postId);
        }
        return rowsAffected;
    }

    /**
     * 根据用户ID和博文ID删除收藏记录（用于取消收藏，逻辑删除）。
     *
     * @param userId 收藏用户的ID。
     * @param postId 被取消收藏博文的ID (MongoDB ID)。
     * @return 成功删除的记录数 (通常为 1)。
     * @throws ResourceNotFoundException 如果收藏记录不存在。
     */
    public int deleteFavorite(Long userId, String postId) {
        QueryWrapper<PostFavorite> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId.toString());
        queryWrapper.eq("post_id", postId);
        queryWrapper.eq("deleted", 0);

        PostFavorite existingFavorite = postFavoriteMapper.selectOne(queryWrapper);
        if (existingFavorite == null) {
            throw new ResourceNotFoundException("用户ID: " + userId + " 未收藏博文ID: " + postId + "。");
        }

        int rowsAffected = postFavoriteMapper.delete(queryWrapper);
        if (rowsAffected > 0) {
            log.info("用户ID: {} 取消收藏博文ID: {}", userId, postId);
        }
        return rowsAffected;
    }

    public Boolean checkFavorite(Long userId, String postId) {
        QueryWrapper<PostFavorite> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        queryWrapper.eq("post_id", postId);
        queryWrapper.eq("deleted", 0);

        Long count = postFavoriteMapper.selectCount(queryWrapper);
        return count > 0;
    }
}