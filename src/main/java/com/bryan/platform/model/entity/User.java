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
    @TableId(type = IdType.INPUT) // 改为 INPUT 类型
    private Long id;

    private String username;

    private String password;  // 实际存储加密后的密码

    private String email;

    private Integer status;   // 状态（0-正常，1-封禁）

    private String roles;

    @TableLogic
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

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

    // 以下是 UserDetails 的其他必要方法
    @Override
    public String getUsername() {
        return this.username;
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return this.status == null || this.status == 0; // 检查status是否为0（正常），或为null（默认正常）
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

}
