package com.bryan.platform.mapper;

import com.bryan.platform.domain.entity.user.SysUser;
import com.bryan.platform.domain.enums.UserStatusEnum;
import com.bryan.platform.domain.request.user.UserExportRequest;
import com.bryan.platform.domain.request.user.UserSearchRequest;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;

/**
 * UserMapper
 *
 * @author Bryan Long
 */
@Mapper
public interface UserMapper {

    int insert(SysUser user);

    SysUser selectById(Long id);

    SysUser selectByUsername(String username);

    List<SysUser> selectPage(@Param("offset") long offset,
                             @Param("pageSize") long pageSize,
                             @Param("req") UserSearchRequest search,
                             @Param("export") UserExportRequest export);

    List<SysUser> selectByIdList(@Param("ids") Collection<Long> ids);

    SysUser selectByStatus(@Param("status") UserStatusEnum status);

    int update(SysUser user);

    int updateDeletedById(@Param("id") Long id, @Param("deleted") Integer deleted);

    long count(@Param("req") UserSearchRequest search,
               @Param("export") UserExportRequest export);
}
