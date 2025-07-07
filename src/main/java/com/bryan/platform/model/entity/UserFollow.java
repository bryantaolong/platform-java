package com.bryan.platform.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * ClassName: UserFollow
 * Package: com.bryan.platform.model.entity
 * Description:
 * Author: Bryan Long
 * Create: 2025/6/23 - 18:47
 * Version: v1.0
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
