CREATE TABLE `user` (
                        `id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户ID',
                        `username` varchar(50) NOT NULL COMMENT '用户名',
                        `password` varchar(100) NOT NULL COMMENT '加密后的密码',
                        `email` varchar(100) DEFAULT NULL COMMENT '邮箱',
                        `status` tinyint DEFAULT '0' COMMENT '状态（0-正常，1-封禁）',
                        roles       varchar(255) DEFAULT 'ROLE_USER' NULL COMMENT '用户角色，多个角色用逗号分隔',
                        `deleted` tinyint DEFAULT '0' COMMENT '逻辑删除标记（0-未删除，1-已删除）',
                        `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                        `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                        PRIMARY KEY (`id`),
                        UNIQUE KEY `idx_username` (`username`) COMMENT '用户名唯一索引',
                        KEY `idx_deleted` (`deleted`) COMMENT '逻辑删除索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户表';
