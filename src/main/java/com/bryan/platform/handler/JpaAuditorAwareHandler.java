package com.bryan.platform.handler;

import com.bryan.platform.model.entity.user.User;
import org.springframework.data.domain.AuditorAware;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * JPA 等价的“创建人/更新人”自动填充器
 *
 * @author Bryan Long
 */
@Component
public class JpaAuditorAwareHandler implements AuditorAware<String> {

    public static final String SYSTEM_USER = "system";

    /**
     * 每次 JPA 需要写入 create_by / update_by 时都会调用此方法
     *
     * @return 当前登录用户名（不可能为 null，至少返回 "system"）
     */
    @Override
    @NonNull
    public Optional<String> getCurrentAuditor() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal() == null) {
            // 匿名或未登录 -> 沿用 MBP 的兜底
            return Optional.of(SYSTEM_USER);
        }

        Object principal = auth.getPrincipal();
        if (principal instanceof User) {
            // 如果 principal 是我们自己的 User 实体
            return Optional.of(((User) principal).getUsername());
        } else if (principal instanceof String) {
            // 如果直接就是用户名字符串
            return Optional.of((String) principal);
        }

        // 兜底
        return Optional.of(SYSTEM_USER);
    }
}
