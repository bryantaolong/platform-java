package com.bryan.platform.domain.entity.user;

import com.baomidou.mybatisplus.annotation.*;
import com.bryan.platform.domain.enums.UserStatusEnum;
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

    private String phone;

    private String email;

    /** 使用枚举 */
    @EnumValue
    private UserStatusEnum status;

    /** 逗号分隔的角色标识 */
    private String roles;

    private LocalDateTime lastLoginAt;

    private String lastLoginIp;

    private LocalDateTime passwordResetAt;

    private Integer loginFailCount; // 登录失败次数

    private LocalDateTime lockedAt; // 账户锁定时间

    /** 逻辑删除 */
    @TableLogic
    private Integer deleted;

    /** 乐观锁 */
    @Version
    private Integer version;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    /** 创建人 */
    @TableField(fill = FieldFill.INSERT)
    private String createdBy;

    /** 更新人 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updatedBy;

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
        // 正常状态直接返回 true
        if (this.status == UserStatusEnum.NORMAL) {
            return true;
        }
        // 锁定状态：判断锁定时间是否已过 1 小时
        if (this.status == UserStatusEnum.LOCKED && this.lockedAt != null) {
            return LocalDateTime.now()
                    .isAfter(this.lockedAt.plusHours(1));
        }
        return false;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return this.status != UserStatusEnum.BANNED && this.deleted == 0;
    }
}
