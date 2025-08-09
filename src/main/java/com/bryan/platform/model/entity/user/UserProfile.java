package com.bryan.platform.model.entity.user;

import com.bryan.platform.model.enums.GenderEnum;
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
    @Column(name = "deleted")
    private Integer deleted = 0;

    @Version
    @Column(name = "version")
    private Integer version = 0;

    @CreatedDate
    @Column(name = "create_time")
    private LocalDateTime createTime;

    @LastModifiedDate
    @Column(name = "update_time")
    private LocalDateTime updateTime;

    @CreatedBy
    @Column(name = "create_by")
    private String createBy;

    @LastModifiedBy
    @Column(name = "update_by")
    private String updateBy;
}
