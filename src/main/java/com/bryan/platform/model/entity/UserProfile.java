package com.bryan.platform.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * userProfile
 *
 * @author Bryan Long
 * @version 1.0
 * @since 2025/8/1
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("\"user_profile\"")
@KeySequence(value = "user_id_seq") // 指定序列名称
public class UserProfile {
    @TableId(type = IdType.INPUT)

    private Long userId;

    private String realName;

    private Integer gender;

    private LocalDateTime birthday;

    private String avatar;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    private String updateBy;
}
