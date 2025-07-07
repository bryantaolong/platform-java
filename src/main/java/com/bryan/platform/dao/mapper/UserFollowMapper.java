package com.bryan.platform.dao.mapper;

import com.bryan.platform.model.entity.User;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * ClassName: UserFollowMapper
 * Package: com.bryan.platform.dao.mapper
 * Description:
 * Author: Bryan Long
 * Create: 2025/6/23 - 18:52
 * Version: v1.0
 */
@Mapper
public interface UserFollowMapper {

    @Insert("INSERT INTO user_follows (follower_id, following_id, create_time) " +
            "VALUES (#{followerId}, #{followingId}, CURRENT_TIMESTAMP)")
    int insertFollow(@Param("followerId") Long followerId,
                     @Param("followingId") Long followingId);

    @Delete("DELETE FROM user_follows " +
            "WHERE follower_id = #{followerId} " +
            "AND following_id = #{followingId}")
    int deleteFollow(@Param("followerId") Long followerId,
                     @Param("followingId") Long followingId);

    @Select("SELECT COUNT(1) FROM user_follows " +
            "WHERE follower_id = #{followerId} " +
            "AND following_id = #{followingId}")
    int existsFollow(@Param("followerId") Long followerId,
                     @Param("followingId") Long followingId);

    @Select("SELECT u.* FROM \"user\" u " +
            "JOIN user_follows uf ON u.id = uf.following_id " +
            "WHERE uf.follower_id = #{userId} " +
            "LIMIT #{limit} OFFSET #{offset}")
    List<User> selectFollowingList(
            @Param("userId") Long userId,
            @Param("offset") long offset,
            @Param("limit") long limit
    );

    @Select("SELECT u.* FROM \"user\" u " +
            "JOIN user_follows uf ON u.id = uf.follower_id " +
            "WHERE uf.following_id = #{userId} " +
            "LIMIT #{limit} OFFSET #{offset}")
    List<User> selectFollowerList(
            @Param("userId") Long userId,
            @Param("offset") long offset,
            @Param("limit") long limit
    );

    @Select("SELECT COUNT(1) FROM user_follows WHERE follower_id = #{userId}")
    long countFollowing(@Param("userId") Long userId);

    @Select("SELECT COUNT(1) FROM user_follows WHERE following_id = #{userId}")
    long countFollowers(@Param("userId") Long userId);
}
