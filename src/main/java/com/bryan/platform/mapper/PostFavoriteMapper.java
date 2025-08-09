package com.bryan.platform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bryan.platform.model.entity.post.PostFavorite;
import org.apache.ibatis.annotations.*;

/**
 * PostFavorite 博文收藏数据访问层
 *
 * @author Bryan Long
 */
@Mapper
public interface PostFavoriteMapper extends BaseMapper<PostFavorite> {
}
