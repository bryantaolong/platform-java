package com.bryan.platform.mapper;

import com.bryan.platform.domain.entity.post.PostFavorite;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * PostFavorite 博文收藏数据访问层
 *
 * @author Bryan Long
 */
@Mapper
public interface PostFavoriteMapper {

    int insert(PostFavorite record);

    List<String> selectPostIdsByUserId(@Param("userId") Long userId);

    int updateDeletedByUserIdAndPostId(@Param("userId") Long userId,
                                       @Param("postId") String postId,
                                       @Param("deleted") Integer deleted);

    long countByUserId(@Param("userId") Long userId);

    long countByUserIdAndPostId(@Param("userId") Long userId,
                                @Param("postId") String postId);
}
