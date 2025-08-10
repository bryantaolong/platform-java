package com.bryan.platform.domain.entity.user;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户关注关系实体
 *
 * @author Bryan Long
 */
@Data
@TableName("user_follows")
@KeySequence(value = "user_follows_id_seq")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserFollow implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long followerId; // 关注者ID

    private Long followingId; // 被关注者ID

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
