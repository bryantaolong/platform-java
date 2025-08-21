package com.bryan.platform.mapper;

import com.bryan.platform.domain.entity.user.UserProfile;
import org.apache.ibatis.annotations.Mapper;

/**
 * UserProfileMapper
 *
 * @author Bryan Long
 */
@Mapper
public interface UserProfileMapper {

    int insert(UserProfile record);

    UserProfile selectByUserId(Long userId);

    UserProfile selectByRealName(String realName);

    int update(UserProfile record);
}
