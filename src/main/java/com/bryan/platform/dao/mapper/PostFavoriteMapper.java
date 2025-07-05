package com.bryan.platform.dao.mapper;

import com.bryan.platform.model.entity.PostFavorite;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * ClassName: FavoriteMapper
 * Package: com.bryan.platform.dao.mapper
 * Description:
 * Author: Bryan Long
 * Create: 2025/6/22 - 16:21
 * Version: v1.0
 */
@Mapper
public interface PostFavoriteMapper {

    /**
     * 查询用户收藏的所有 postId（未逻辑删除的）
     */
    @Select("SELECT post_id FROM post_favorite WHERE user_id = #{userId} AND deleted = 0")
    List<String> selectPostIdsByUserId(@Param("userId") Long userId);

    /**
     * 查询某用户是否收藏过某篇文章（不管是否逻辑删除）
     */
    @Select("SELECT * FROM post_favorite WHERE user_id = #{userId} AND post_id = #{postId} LIMIT 1")
    PostFavorite selectByUserIdAndPostId(@Param("userId") Long userId, @Param("postId") String postId);

    /**
     * 查询某用户是否有效收藏某篇文章（deleted = 0）
     */
    @Select("SELECT * FROM post_favorite WHERE user_id = #{userId} AND post_id = #{postId} AND deleted = 0 LIMIT 1")
    PostFavorite selectActiveByUserIdAndPostId(@Param("userId") Long userId, @Param("postId") String postId);

    /**
     * 插入收藏记录
     */
    @Insert("INSERT INTO post_favorite (user_id, post_id, deleted, create_time, update_time) " +
            "VALUES (#{userId}, #{postId}, #{deleted}, #{createTime}, #{updateTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(PostFavorite favorite);

    /**
     * 更新收藏记录的 deleted 字段和更新时间
     */
    @Update("UPDATE post_favorite SET deleted = #{deleted}, update_time = #{updateTime} " +
            "WHERE user_id = #{userId} AND post_id = #{postId}")
    int update(PostFavorite favorite);

    /**
     * 逻辑删除收藏（即设置 deleted = 1）
     */
    @Update("UPDATE post_favorite SET deleted = 1, update_time = NOW() WHERE user_id = #{userId} AND post_id = #{postId} AND deleted = 0")
    int logicDelete(@Param("userId") Long userId, @Param("postId") String postId);

    /**
     * 查询是否已收藏（用于 checkFavorite）
     */
    @Select("SELECT COUNT(*) FROM post_favorite WHERE user_id = #{userId} AND post_id = #{postId} AND deleted = 0")
    Long countActiveFavorite(@Param("userId") Long userId, @Param("postId") String postId);
}
