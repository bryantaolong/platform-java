package com.bryan.platform.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
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
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("\"user\"")
@KeySequence(value = "user_id_seq") // 指定序列名称
public class User implements Serializable, UserDetails {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String username;

    private String password;

    private String phoneNumber;

    private String email;

    private Integer status; // 状态（0-正常，1-封禁，2-锁定）

    private String roles; // 角色标识，多个用英文逗号分隔

    private LocalDateTime loginTime;

    private String loginIp;

    private LocalDateTime passwordResetTime;

    private Integer loginFailCount; // 登录失败次数

    private LocalDateTime accountLockTime; // 账户锁定时间

    @TableLogic
    private Integer deleted;

    @Version
    private Integer version; // 乐观锁版本号

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    private String createBy;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    private String updateBy;

    /**
     * 获取用户权限（Spring Security要求）。
     * 根据 roles 字段解析权限。
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (this.roles == null || this.roles.isEmpty()) {
            // 如果没有设置角色，可以返回一个默认角色，或者空列表
            return Collections.emptyList();
        }
        // 将逗号分隔的角色字符串转换为 SimpleGrantedAuthority 列表
        return Arrays.stream(this.roles.split(","))
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        // status为0且未达到锁定时间或锁定时间已过
        if (this.status == 0) return true;
        if (this.status == 2 && this.accountLockTime != null) {
            return LocalDateTime.now().isAfter(this.accountLockTime.plusHours(1));
        }
        return false;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return this.status != 1 && this.deleted == 0;
    }
}
