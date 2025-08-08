package com.bryan.platform.model.entity.user;

import com.bryan.platform.common.enums.UserStatusEnum;
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
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

/**
 * User 用户实体类，实现 UserDetails 接口用于 Spring Security。
 *
 * @author Bryan Long
 * @version 1.0
 * @since 2025/6/19 - 19:45
 */
@Entity
@Table(name = "\"user\"")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@SQLRestriction("deleted = 0")                       // 逻辑删除过滤
@SQLDelete(sql = "UPDATE \"user\" SET deleted = 1, update_time = NOW() WHERE id = ? AND version = ?")
@EntityListeners(AuditingEntityListener.class)        // 自动填充审计字段
public class User implements Serializable, UserDetails {

    /* ---------- 主键 ---------- */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_user_id_seq")
    @SequenceGenerator(name = "user_user_id_seq", sequenceName = "user_id_seq", allocationSize = 1)
    private Long id;

    /* ---------- 业务字段 ---------- */
    @Column(unique = true, nullable = false, length = 64)
    private String username;

    @Column(nullable = false, length = 256)
    private String password;

    @Column(length = 20)
    private String phoneNumber;

    @Column(unique = true, nullable = false, length = 128)
    private String email;

    @Enumerated(EnumType.ORDINAL)   // 与数据库 integer 列对应
    @Column(name = "status", nullable = false)
    private UserStatusEnum status = UserStatusEnum.NORMAL;

    /** 逗号分隔的角色标识 */
    @Column(length = 512)
    private String roles;

    private LocalDateTime loginTime;

    private String loginIp;

    private LocalDateTime passwordResetTime;


    private Integer loginFailCount = 0;

    private LocalDateTime accountLockTime;

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

    /* ---------- UserDetails 实现 ---------- */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (roles == null || roles.isEmpty()) {
            return Collections.emptyList();
        }
        return Arrays.stream(roles.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        if (status == UserStatusEnum.NORMAL) return true;
        if (status == UserStatusEnum.LOCKED && accountLockTime != null) {
            return LocalDateTime.now().isAfter(accountLockTime.plusHours(1));
        }
        return false;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return status != UserStatusEnum.BANNED && deleted == 0;
    }
}
