package com.bryan.platform.domain.entity.user;

import com.bryan.platform.domain.enums.GenderEnum;
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
public class UserProfile {

    private Long userId;

    private String realName;

    private GenderEnum gender;

    private LocalDateTime birthday;

    private String avatar;

    /** 逻辑删除 */
    private Integer deleted;

    /** 乐观锁 */
    private Integer version;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 更新时间 */
    private LocalDateTime updatedAt;

    /** 创建人 */
    private String createdBy;

    /** 更新人 */
    private String updatedBy;
}
