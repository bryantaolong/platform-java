package com.bryan.platform.handler;

import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.Properties;

/**
 * 自动填充审计字段的 MyBatis 拦截器
 */
@Intercepts({
        @Signature(type = Executor.class,
                method  = "update",
                args    = {MappedStatement.class, Object.class})
})
public class AuditFieldInterceptor implements Interceptor {

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        MappedStatement ms = (MappedStatement) invocation.getArgs()[0];
        Object parameter   = invocation.getArgs()[1];

        // 只处理 INSERT / UPDATE
        if (ms.getSqlCommandType() == SqlCommandType.INSERT) {
            fillInsert(parameter);
        } else if (ms.getSqlCommandType() == SqlCommandType.UPDATE) {
            fillUpdate(parameter);
        }
        return invocation.proceed();
    }

    private void fillInsert(Object param) {
        // param 可能是单个实体，也可能是 Map
        MetaObject meta = SystemMetaObject.forObject(param);
        LocalDateTime now = LocalDateTime.now();
        setValue(meta, "createdAt", now);
        setValue(meta, "updatedAt", now);
        setValue(meta, "createdBy", currentUser());
        setValue(meta, "updatedBy", currentUser());
    }

    private void fillUpdate(Object param) {
        MetaObject meta = SystemMetaObject.forObject(param);
        setValue(meta, "updatedAt", LocalDateTime.now());
        setValue(meta, "updatedBy", currentUser());
    }

    private void setValue(MetaObject meta, String property, Object value) {
        if (meta.hasSetter(property)) {
            meta.setValue(property, value);
        }
    }

    private String currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return "system";
        }
        return auth.getName();
    }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) { }
}