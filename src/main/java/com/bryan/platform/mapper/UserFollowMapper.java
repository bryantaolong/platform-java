package com.bryan.platform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bryan.platform.domain.entity.user.UserFollow;
import org.apache.ibatis.annotations.*;

/**
 * UserFollow 用户关注数据访问层
 *
 * @author Bryan Long
 */
@Mapper
public interface UserFollowMapper extends BaseMapper<UserFollow> {
}
