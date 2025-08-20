package com.bryan.platform.mapper;

import com.bryan.platform.domain.entity.user.UserFollow;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * UserFollow 用户关注数据访问层
 *
 * @author Bryan Long
 */
@Mapper
public interface UserFollowMapper {

    int insert(UserFollow record);

    UserFollow selectById(Long id);

    List<UserFollow> selectPageByFollowerId(@Param("followerId") Long followerId,
                                            @Param("offset") long offset,
                                            @Param("size") int size);

    List<UserFollow> selectPageByFollowingId(@Param("followingId") Long followingId,
                                             @Param("offset") long offset,
                                             @Param("size") int size);

    long countByFollowerId(@Param("followerId") Long followerId);

    int updateDeletedByFollowerIdAndFollowingId(@Param("followerId") Long followerId,
                                                @Param("followingId") Long followingId,
                                                @Param("deleted") Integer deleted);

    long countByFollowerIdAndFollowingId(@Param("followerId") Long followerId,
                                         @Param("followingId") Long followingId);

    long countByFollowingId(@Param("followingId") Long followingId);
}
