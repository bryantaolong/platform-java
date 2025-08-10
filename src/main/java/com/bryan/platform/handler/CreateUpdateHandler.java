package com.bryan.platform.handler;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.bryan.platform.domain.entity.user.User;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * CreateUpdateHandler MyBatis-Plus 自动填充处理器
 *
 * @author Bryan Long
 */
@Component
public class CreateUpdateHandler implements MetaObjectHandler {

    /**
     * 在插入操作时自动填充 createdAt 和 updatedAt 字段
     * 在插入时填充 createdBy 和 updatedBy
     */
    @Override
    public void insertFill(MetaObject metaObject) {
        this.strictInsertFill(metaObject, "createdAt", LocalDateTime.class, LocalDateTime.now());
        this.strictInsertFill(metaObject, "updatedAt", LocalDateTime.class, LocalDateTime.now());

        String currentUsername = getCurrentUsername(); // 从上下文中获取当前用户
        this.strictInsertFill(metaObject, "createdBy", String.class, currentUsername);
        this.strictInsertFill(metaObject, "updatedBy", String.class, currentUsername);
    }

    /**
     * 在更新操作时自动填充 updatedAt 字段
     * 在更新时填充 updatedBy
     */
    @Override
    public void updateFill(MetaObject metaObject) {
        this.strictUpdateFill(metaObject, "updatedAt", LocalDateTime.class, LocalDateTime.now());

        String currentUsername = getCurrentUsername(); // 从上下文中获取当前用户
        this.strictUpdateFill(metaObject, "updatedBy", String.class, currentUsername);
    }

    /**
     * 从 Spring Security 或其他上下文获取当前登录用户名
     */
    private String getCurrentUsername() {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return currentUser.getUsername();
    }
}
