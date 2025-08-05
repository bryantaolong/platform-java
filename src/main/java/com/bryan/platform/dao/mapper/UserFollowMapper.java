package com.bryan.platform.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bryan.platform.model.entity.user.UserFollow;
import org.apache.ibatis.annotations.*;

/**
 * UserFollow 用户关注数据访问层
 *
 * @author Bryan Long
 * @version 1.0
 * @since 2025/6/23 - 18:52
 */
@Mapper
public interface UserFollowMapper extends BaseMapper<UserFollow> {
}
