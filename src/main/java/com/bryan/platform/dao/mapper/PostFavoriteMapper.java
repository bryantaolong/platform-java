package com.bryan.platform.dao.mapper;

import com.bryan.platform.model.entity.PostFavorite;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * PostFavorite 博文收藏数据访问层
 *
 * @author Bryan Long
 * @version 1.0
 * @since 2025/6/22 - 16:21
 */
@Mapper
public interface PostFavoriteMapper {

    @Select("SELECT post_id FROM post_favorite WHERE user_id = #{userId} AND deleted = 0")
    List<String> selectPostIdsByUserId(@Param("userId") Long userId);

    @Select("SELECT * FROM post_favorite WHERE user_id = #{userId} AND post_id = #{postId} LIMIT 1")
    PostFavorite selectByUserIdAndPostId(@Param("userId") Long userId, @Param("postId") String postId);

    @Select("SELECT * FROM post_favorite WHERE user_id = #{userId} AND post_id = #{postId} AND deleted = 0 LIMIT 1")
    PostFavorite selectActiveByUserIdAndPostId(@Param("userId") Long userId, @Param("postId") String postId);

    @Insert("INSERT INTO post_favorite (user_id, post_id, deleted, create_time, update_time) " +
            "VALUES (#{userId}, #{postId}, #{deleted}, #{createTime}, #{updateTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(PostFavorite favorite);

    @Update("UPDATE post_favorite SET deleted = #{deleted}, update_time = #{updateTime} " +
            "WHERE user_id = #{userId} AND post_id = #{postId}")
    int update(PostFavorite favorite);

    @Update("UPDATE post_favorite SET deleted = 1, update_time = CURRENT_TIMESTAMP " +
            "WHERE user_id = #{userId} AND post_id = #{postId} AND deleted = 0")
    int logicDelete(@Param("userId") Long userId, @Param("postId") String postId);

    @Select("SELECT COUNT(*) FROM post_favorite WHERE user_id = #{userId} AND post_id = #{postId} AND deleted = 0")
    Long countActiveFavorite(@Param("userId") Long userId, @Param("postId") String postId);
}
