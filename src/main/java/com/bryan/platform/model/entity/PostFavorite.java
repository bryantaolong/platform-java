package com.bryan.platform.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * ClassName: PostFavorite
 * Package: com.bryan.platform.model.entity
 * Description: 博文收藏实体类。
 * 记录用户对特定博文的收藏行为。存储在 MySQL 数据库。
 * Author: Bryan Long
 * Create: 2025/6/22 - 16:14
 * Version: v1.0
 */
@Data
@TableName("post_favorite") // 映射到数据库表 post_favorite
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PostFavorite implements Serializable {

    // 收藏记录的唯一ID。由于是 String 类型，且没有 IdType.AUTO，
    // 需要在业务层手动生成（例如 UUID）或依赖数据库自动生成。
    @TableId
    private String id;

    // 收藏用户的ID。MySQL User 的 ID 是 Long，这里存储为 String 类型，
    // 方便与 Post (MongoDB) 的 ID 类型匹配，并可直接存储 UUID 如果 userId 也是 UUID。
    // 在业务层需要进行 Long <-> String 的转换。
    @TableField("user_id") // 明确映射数据库字段名
    private String userId;

    // 被收藏博文的ID。Post (MongoDB) 的 ID 是 String 类型。
    @TableField("post_id") // 明确映射数据库字段名
    private String postId;

    // 逻辑删除字段。使用 MyBatis-Plus 的 @TableLogic 注解，实现数据软删除。
    @TableLogic
    @TableField("deleted") // 明确映射数据库字段名
    private Integer deleted; // 0-未删除，1-已删除

    // 记录创建时间，使用 MyBatis-Plus 的字段填充功能自动填充。
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    // 记录更新时间，使用 MyBatis-Plus 的字段填充功能自动填充。
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
