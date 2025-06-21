package com.bryan.platform.config.handler;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * ClassName: CreateUpdateTimeHandler
 * Package: com.bryan.platform.config.handler
 * Description:
 * Author: Bryan Long
 * Create: 2025/6/19 - 20:01
 * Version: v1.0
 */
@Slf4j
@Component
public class CreateUpdateTimeHandler implements MetaObjectHandler {

    /**
     * 在插入操作时自动填充 createTime 和 updateTime 字段
     */
    @Override
    public void insertFill(MetaObject metaObject) {
        log.info("在插入操作时自动填充 createTime 和 updateTime 字段");
        this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, LocalDateTime.now());
        this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
    }

    /**
     * 在更新操作时自动填充 updateTime 字段
     */
    @Override
    public void updateFill(MetaObject metaObject) {
        log.info("在更新操作时自动填充 updateTime 字段");
        this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
    }
}
