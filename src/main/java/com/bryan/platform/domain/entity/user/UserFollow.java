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
 * 用户关注关系实体
 *
 * @author Bryan Long
 */
@Entity
@Table(name = "user_follows")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@SQLRestriction("deleted = 0")                       // 逻辑删除过滤
@SQLDelete(sql = "UPDATE user_follows SET deleted = 1, update_time = NOW() WHERE id = ? AND version = ?")
@EntityListeners(AuditingEntityListener.class)        // 自动填充审计字段
public class UserFollow implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long followerId; // 关注者ID

    private Long followingId; // 被关注者ID

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
