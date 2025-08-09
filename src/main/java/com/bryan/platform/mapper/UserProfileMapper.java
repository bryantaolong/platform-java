package com.bryan.platform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bryan.platform.model.entity.user.UserProfile;
import org.apache.ibatis.annotations.Mapper;

/**
 * UserProfileMapper
 *
 * @author Bryan Long
 */
@Mapper
public interface UserProfileMapper extends BaseMapper<UserProfile> {
}
