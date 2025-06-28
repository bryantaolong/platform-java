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

    @Select("SELECT * FROM user WHERE username = #{username}")
    User selectByUsername(@Param("username") String username);

    /**
     * 根据状态查询用户列表用于导出
     */
    @Select("<script>" +
            "SELECT id, username, email, roles, status, " +
            "DATE_FORMAT(create_time, '%Y-%m-%d %H:%i:%s') as createTime, " +
            "DATE_FORMAT(update_time, '%Y-%m-%d %H:%i:%s') as updateTime " +
            "FROM user " +
            "WHERE deleted = 0 " +
            "<if test='status != null'> AND status = #{status}</if> " +
            "ORDER BY create_time DESC" +
            "</script>")
    List<Map<String, Object>> selectUsersForExport(@Param("status") Integer status);
}
