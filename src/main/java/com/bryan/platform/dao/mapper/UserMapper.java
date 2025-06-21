package com.bryan.platform.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bryan.platform.model.entity.User;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * ClassName: UserMapper
 * Package: com.bryan.platform.mapper
 * Description:
 * Author: Bryan Long
 * Create: 2025/6/19 - 19:49
 * Version: v1.0
 */
public interface UserMapper extends BaseMapper<User> {
    // 自定义查询示例（如按用户名查找）
    @Select("SELECT * FROM user WHERE username = #{username}")
    User selectByUsername(@Param("username") String username);
}
