package com.bryan.platform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bryan.platform.model.entity.user.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * UserMapper
 *
 * @author Bryan Long
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
}
