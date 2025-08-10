package com.bryan.platform.domain.entity.post;

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
 */
@Data
@TableName("post_favorite")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PostFavorite implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String postId;

    /** 逻辑删除 */
    @TableLogic
    private Integer deleted;

    /** 乐观锁 */
    @Version
    private Integer version;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    /** 创建人 */
    @TableField(fill = FieldFill.INSERT)
    private String createdBy;

    /** 更新人 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updatedBy;
}
