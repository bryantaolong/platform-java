package com.bryan.platform.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户关注关系实体
 *
 * @author Bryan Long
 * @since 2025/6/23 - 18:47
 * @version 1.0
 */
@Data
@TableName("user_follows")
@KeySequence(value = "user_follows_id_seq")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserFollow implements Serializable {
    @TableId(type = IdType.INPUT) // 改为 INPUT 类型
    private Long id;

    private Long followerId; // 关注者ID

    private Long followingId; // 被关注者ID

    @TableField(fill = FieldFill.INSERT)
    private Date createTime;  // 关注时间
}
