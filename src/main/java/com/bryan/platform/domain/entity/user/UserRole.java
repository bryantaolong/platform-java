package com.bryan.platform.domain.entity.user;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * BaseEntity
 *
 * @author Bryan Long
 */
@Entity
@Table(name = "user_role")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@SQLRestriction("deleted = 0")
@SQLDelete(sql = "UPDATE user_role SET deleted = 1, update_time = NOW() WHERE id = ? AND version = ?")
@EntityListeners(AuditingEntityListener.class)
public class UserRole implements Serializable {

    /* ---------- 主键 ---------- */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String roleName;

    @Column(name = "is_default", nullable = false)
    private Boolean isDefault = false;

    /* ---------- 通用字段 ---------- */
    private Integer deleted = 0;

    @Version
    private Integer version = 0;

    @CreatedDate
    private LocalDateTime createTime;

    @LastModifiedDate
    private LocalDateTime updateTime;

    @CreatedBy
    private String createBy;

    @LastModifiedBy
    private String updateBy;
}
