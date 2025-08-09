package com.bryan.platform.domain.entity;

import jakarta.persistence.*;
import lombok.*;
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
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@SQLRestriction("deleted = 0")
//@SQLDelete(sql = "UPDATE table_name SET deleted = 1, update_time = NOW() WHERE id = ? AND version = ?")
@EntityListeners(AuditingEntityListener.class)
public class BaseEntity implements Serializable {

    /* ---------- 主键 ---------- */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

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
