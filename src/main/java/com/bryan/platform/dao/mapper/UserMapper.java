package com.bryan.platform.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bryan.platform.model.entity.User;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * ClassName: UserMapper
 * Package: com.bryan.platform.mapper
 * Description:
 * Author: Bryan Long
 * Create: 2025/6/19 - 19:49
 * Version: v1.0
 */
public interface UserMapper extends BaseMapper<User> {

    @Select("SELECT * FROM \"user\" WHERE username = #{username}")
    User selectByUsername(@Param("username") String username);

    @Select("<script>" +
            "SELECT id, username, email, roles, status, " +
            "to_char(create_time, 'YYYY-MM-DD HH24:MI:SS') as createTime, " +
            "to_char(update_time, 'YYYY-MM-DD HH24:MI:SS') as updateTime " +
            "FROM \"user\" " +
            "WHERE deleted = 0 " +
            "<if test='status != null'> AND status = #{status}</if> " +
            "ORDER BY create_time DESC" +
            "</script>")
    List<Map<String, Object>> selectUsersForExport(@Param("status") Integer status);
}
