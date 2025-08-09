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

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;  // 关注时间
}
