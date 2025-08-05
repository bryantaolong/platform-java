package com.bryan.platform.model.entity.user;

import com.baomidou.mybatisplus.annotation.*;
import com.bryan.platform.common.enums.GenderEnum;
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
    @TableId(type = IdType.ASSIGN_ID)
    private Long userId;

    private String realName;

    @EnumValue
    private GenderEnum gender;

    private LocalDateTime birthday;

    private String avatar;

    /** 逻辑删除 */
    @TableLogic
    private Integer deleted;

    /** 乐观锁 */
    @Version
    private Integer version;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /** 创建人 */
    @TableField(fill = FieldFill.INSERT)
    private String createBy;

    /** 更新人 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updateBy;
}
