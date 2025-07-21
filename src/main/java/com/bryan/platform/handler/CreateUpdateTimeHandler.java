package com.bryan.platform.handler;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * MyBatis-Plus 自动填充处理器
 *
 * @author Bryan Long
 * @since 2025/6/19 - 20:01
 * @version 1.0
 */
@Component
public class CreateUpdateTimeHandler implements MetaObjectHandler {

    /**
     * 在插入操作时自动填充 createTime 和 updateTime 字段
     */
    @Override
    public void insertFill(MetaObject metaObject) {
//        log.info("在插入操作时自动填充 createTime 和 updateTime 字段");
        this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, LocalDateTime.now());
        this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
    }

    /**
     * 在更新操作时自动填充 updateTime 字段
     */
    @Override
    public void updateFill(MetaObject metaObject) {
//        log.info("在更新操作时自动填充 updateTime 字段");
        this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
    }
}
