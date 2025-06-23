package com.bryan.platform.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bryan.platform.model.entity.PostFavorite;
import org.apache.ibatis.annotations.Mapper;

/**
 * ClassName: FavoriteMapper
 * Package: com.bryan.platform.dao.mapper
 * Description:
 * Author: Bryan Long
 * Create: 2025/6/22 - 16:21
 * Version: v1.0
 */
@Mapper
public interface FavoriteMapper extends BaseMapper<PostFavorite> {
}
