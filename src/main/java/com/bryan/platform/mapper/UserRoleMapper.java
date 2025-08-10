package com.bryan.platform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bryan.platform.domain.entity.user.UserRole;
import org.apache.ibatis.annotations.Mapper;

/**
 * UserRoleMapper
 *
 * @author Bryan Long
 */
@Mapper
public interface UserRoleMapper extends BaseMapper<UserRole> {
}
