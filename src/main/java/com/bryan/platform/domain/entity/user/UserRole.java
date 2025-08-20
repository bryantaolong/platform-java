package com.bryan.platform.domain.entity.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * BaseEntity
 *
 * @author Bryan Long
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRole implements Serializable {

    private Long id;

    private String roleName;

    private Boolean isDefault;

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
