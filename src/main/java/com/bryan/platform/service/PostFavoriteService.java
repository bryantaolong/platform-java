package com.bryan.platform.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.bryan.platform.common.exception.BusinessException;
import com.bryan.platform.common.exception.ResourceNotFoundException;
import com.bryan.platform.dao.mapper.FavoriteMapper;
import com.bryan.platform.dao.repository.PostRepository;
import com.bryan.platform.model.entity.Post;
import com.bryan.platform.model.entity.PostFavorite;
import com.bryan.platform.model.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page; // Spring Data Page
import org.springframework.data.domain.Pageable; // Spring Data Pageable
import org.springframework.data.domain.PageImpl; // For creating an empty page
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * ClassName: PostFavoriteServiceImpl
 * Package: com.bryan.platform.service.impl
 * Description: 博文收藏服务实现类。
 * 处理博文收藏的增删查业务逻辑。
 * Author: Bryan Long
 * Create: 2025/6/22 - 16:22
 * Version: v1.0
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class) // 启用事务管理，遇到异常回滚
@RequiredArgsConstructor
public class PostFavoriteService {

    private final FavoriteMapper favoriteMapper;

    private final PostRepository postRepository;

    private final UserService userService;

    /**
     * 根据用户ID获取其收藏的博文列表（分页）。
     *
     * @param userId   用户的ID。
     * @param pageable 分页信息。
     * @return 该用户收藏的博文分页列表。
     * @throws ResourceNotFoundException 如果用户不存在。
     */
    public Page<Post> getFavoritePostsByUserId(Long userId, Pageable pageable) {
        // 1. 检查用户是否存在
        User user = userService.getUserById(userId);
        if (user == null) {
            throw new ResourceNotFoundException("用户未找到，ID: " + userId);
        }

        // 2. 从 MySQL 获取该用户所有收藏的 post_id 列表
        QueryWrapper<PostFavorite> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId.toString());
        queryWrapper.eq("deleted", 0); // 确保只查询未删除的收藏记录
        queryWrapper.select("post_id"); // 只查询 post_id 字段以优化性能

        List<PostFavorite> favoriteEntities = favoriteMapper.selectList(queryWrapper);

        // 提取所有收藏的 post_id 列表
        List<String> favoritePostIds = favoriteEntities.stream()
                .map(PostFavorite::getPostId)
                .collect(Collectors.toList());

        // 3. 如果没有收藏的博文，则直接返回空分页结果
        if (favoritePostIds.isEmpty()) {
            return new PageImpl<>(Collections.emptyList(), pageable, 0);
        }

        // 4. 根据收藏的 post_id 列表和分页信息，从 MongoDB 查询博文
        // 此处调用 PostRepository 中新增的 findByIdIn 方法，该方法会在 MongoDB 中进行分页和排序
        return postRepository.findByIdIn(favoritePostIds, pageable);
    }

    /**
     * 添加博文到用户的收藏列表。
     *
     * @param userId 收藏用户的ID。
     * @param postId 被收藏博文的ID (MongoDB ID)。
     * @return 成功添加的收藏记录数 (通常为 1)。
     * @throws ResourceNotFoundException 如果用户或博文不存在。
     * @throws BusinessException 如果博文已被收藏。
     */
    public int addFavorite(Long userId, String postId) {
        // 1. 校验用户是否存在
        User user = userService.getUserById(userId);
        if (user == null) {
            throw new ResourceNotFoundException("收藏用户未找到，ID: " + userId);
        }

        // 2. 校验博文是否存在（在 MongoDB 中）
        Optional<Post> postOptional = postRepository.findById(postId);
        if (postOptional.isEmpty()) {
            throw new ResourceNotFoundException("被收藏博文未找到，ID: " + postId);
        }

        // 3. 检查是否已经收藏过
        QueryWrapper<PostFavorite> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId.toString());
        queryWrapper.eq("post_id", postId);
        queryWrapper.eq("deleted", 0); // 确保未删除
        Long existingFavoriteCount = favoriteMapper.selectCount(queryWrapper);

        if (existingFavoriteCount > 0) {
            throw new BusinessException("该博文已被收藏，无需重复收藏。");
        }

        // 4. 创建收藏记录
        PostFavorite favorite = PostFavorite.builder()
                .id(UUID.randomUUID().toString()) // 生成唯一 String ID
                .userId(userId.toString()) // 将 Long 类型的 userId 转换为 String
                .postId(postId)
                .deleted(0) // 默认未删除
                .build();

        int rowsAffected = favoriteMapper.insert(favorite);
        if (rowsAffected > 0) {
            log.info("用户ID: {} 收藏博文ID: {}", userId, postId);
        }
        return rowsAffected;
    }

    /**
     * 根据收藏记录的ID删除收藏（逻辑删除）。
     *
     * @param postFavoriteId 收藏记录的唯一ID。
     * @return 成功删除的记录数 (通常为 1)。
     * @throws ResourceNotFoundException 如果收藏记录不存在。
     */
    public int deleteFavoriteById(String postFavoriteId) {
        PostFavorite existingFavorite = favoriteMapper.selectById(postFavoriteId);
        if (existingFavorite == null || existingFavorite.getDeleted() == 1) { // 检查是否已存在且未删除
            throw new ResourceNotFoundException("收藏记录未找到或已删除，ID: " + postFavoriteId);
        }

        int rowsAffected = favoriteMapper.deleteById(postFavoriteId); // MyBatis-Plus 会根据 @TableLogic 执行逻辑删除
        if (rowsAffected > 0) {
            log.info("删除收藏记录ID: {}", postFavoriteId);
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
    public int deleteFavoriteByUserIdAndPostId(Long userId, String postId) {
        QueryWrapper<PostFavorite> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId.toString());
        queryWrapper.eq("post_id", postId);
        queryWrapper.eq("deleted", 0); // 只删除未逻辑删除的记录

        // 先查询，确保记录存在
        PostFavorite existingFavorite = favoriteMapper.selectOne(queryWrapper);
        if (existingFavorite == null) {
            throw new ResourceNotFoundException("用户ID: " + userId + " 未收藏博文ID: " + postId + "。");
        }

        int rowsAffected = favoriteMapper.delete(queryWrapper); // MyBatis-Plus 执行逻辑删除
        if (rowsAffected > 0) {
            log.info("用户ID: {} 取消收藏博文ID: {}", userId, postId);
        }
        return rowsAffected;
    }

    /**
     * 检查当前用户是否已收藏指定博文。
     *
     * @param postId 要检查的博文ID (MongoDB ID)。
     * @return 如果已收藏返回 true，否则返回 false。
     */
    public Boolean checkFavorite(Long userId, String postId) {
        QueryWrapper<PostFavorite> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        queryWrapper.eq("post_id", postId);
        queryWrapper.eq("deleted", 0); // 只检查未逻辑删除的记录

        Long count = favoriteMapper.selectCount(queryWrapper);
        return count > 0;
    }

    public Boolean checkFavoriteOwnership(String favoriteId, Long userId) {
        QueryWrapper<PostFavorite> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId.toString());
        queryWrapper.eq("favoriteId", favoriteId);
        queryWrapper.eq("deleted", 0);
        Long count = favoriteMapper.selectCount(queryWrapper);
        return count > 0;
    }
}