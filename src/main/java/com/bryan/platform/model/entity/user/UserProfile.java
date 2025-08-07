package com.bryan.platform.model.entity.user;

import com.bryan.platform.common.enums.GenderEnum;
import jakarta.persistence.*;
import jakarta.persistence.Version;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * userProfile
 *
 * @author Bryan Long
 * @version 1.0
 * @since 2025/8/1
 */
@Entity
@Table(name = "user_profile")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@SQLRestriction("deleted = 0")
@SQLDelete(sql = "UPDATE user_profile SET deleted = 1, update_time = NOW() WHERE user_id = ? AND version = ?")
@EntityListeners(AuditingEntityListener.class) // 自动填充审计字段
public class UserProfile {

    /* ---------- 主键 ---------- */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_id_seq")
    @SequenceGenerator(name = "user_id_seq", sequenceName = "user_id_seq", allocationSize = 1)
    private Long userId;

    /* ---------- 业务字段 ---------- */
    private String realName;

    @Enumerated(EnumType.ORDINAL)
    @Column(name = "gender", nullable = false)
    private GenderEnum gender;

    private LocalDateTime birthday;

    private String avatar;

    /* ---------- 通用字段 ---------- */
    // 逻辑删除
    private Integer deleted = 0;

    // 乐观锁
    @Version
    private Integer version;

    // === 审计字段 ===
    @CreatedDate
    private LocalDateTime createTime;

    @LastModifiedDate
    private LocalDateTime updateTime;

    @CreatedBy
    private String createBy;

    @LastModifiedBy
    private String updateBy;
}
