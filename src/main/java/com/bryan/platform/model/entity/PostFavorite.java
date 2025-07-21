package com.bryan.platform.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 博文收藏实体类
 * 记录用户对特定博文的收藏行为。存储在 PostgreSQL 数据库。
 *
 * @author Bryan Long
 * @since 2025/6/22 - 16:14
 * @version 2.0
 */
@Data
@TableName("post_favorite")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PostFavorite implements Serializable {
    // 用户ID，复合主键的一部分
    private Long userId;

    // 博文ID，复合主键的一部分
    private String postId;

    @TableLogic
    @TableField("deleted")
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}