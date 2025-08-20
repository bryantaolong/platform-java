package com.bryan.platform.mapper;

import com.bryan.platform.domain.entity.user.UserRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;

/**
 * UserRoleMapper
 *
 * @author Bryan Long
 */
@Mapper
public interface UserRoleMapper{

    UserRole selectOneByIsDefaultTrue();

    List<UserRole> selectAll();

    List<UserRole> selectByIdList(@Param("ids") Collection<Long> ids);
}
