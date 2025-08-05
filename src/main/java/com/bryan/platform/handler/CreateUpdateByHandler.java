package com.bryan.platform.handler;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.bryan.platform.model.entity.user.User;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * MyBatis-Plus 自动填充创建人/更新人处理器
 *
 * @author Bryan Long
 * @since 2025/6/19 - 20:01
 * @version 1.0
 */
@Component
public class CreateUpdateByHandler implements MetaObjectHandler {

    /**
     * 在插入时填充 createBy 和 updateBy
     */
    @Override
    public void insertFill(MetaObject metaObject) {
        String currentUsername = getCurrentUsername(); // 从上下文中获取当前用户
        this.strictInsertFill(metaObject, "createBy", String.class, currentUsername);
        this.strictInsertFill(metaObject, "updateBy", String.class, currentUsername);
    }

    /**
     * 在更新时填充 updateBy
     */
    @Override
    public void updateFill(MetaObject metaObject) {
        String currentUsername = getCurrentUsername(); // 从上下文中获取当前用户
        this.strictUpdateFill(metaObject, "updateBy", String.class, currentUsername);
    }

    /**
     * 从 Spring Security 或其他上下文获取当前登录用户名
     */
    private String getCurrentUsername() {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return currentUser.getUsername();
    }
}
