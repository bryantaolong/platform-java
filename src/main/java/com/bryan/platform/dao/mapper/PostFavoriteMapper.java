package com.bryan.platform.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bryan.platform.model.entity.PostFavorite;
import org.apache.ibatis.annotations.*;

/**
 * PostFavorite 博文收藏数据访问层
 *
 * @author Bryan Long
 * @version 1.0
 * @since 2025/6/22 - 16:21
 */
@Mapper
public interface PostFavoriteMapper extends BaseMapper<PostFavorite> {
}
