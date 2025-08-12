package com.bryan.platform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bryan.platform.domain.entity.user.UserFollow;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * UserFollow 用户关注数据访问层
 *
 * @author Bryan Long
 */
@Mapper
public interface UserFollowMapper extends BaseMapper<UserFollow> {

//    int insert(UserFollow record);

//    UserFollow selectById(Long id);

    List<UserFollow> selectPageByFollowerId(@Param("followerId") Long followerId,
                                            @Param("offset") long offset,
                                            @Param("size") int size);

    long countByFollowerId(@Param("followerId") Long followerId);

    int deleteByFollowerIdAndFollowingId(@Param("followerId") Long followerId,
                                         @Param("followingId") Long followingId);

    long countByFollowerIdAndFollowingId(@Param("followerId") Long followerId,
                                         @Param("followingId") Long followingId);

    long countByFollowingId(@Param("followingId") Long followingId);
}
