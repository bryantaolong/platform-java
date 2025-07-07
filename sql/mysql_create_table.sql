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

create table post_favorite
(
    user_id     varchar(64)                        not null comment '用户ID',
    post_id     varchar(64)                        not null comment '博文ID (MongoDB ObjectId)',
    deleted     tinyint  default 0                 null comment '逻辑删除: 0-未删除, 1-已删除',
    create_time datetime default CURRENT_TIMESTAMP null comment '创建时间',
    update_time datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    primary key (user_id, post_id) comment '复合主键: user_id + post_id'
)
    comment '博文收藏记录表';

-- 保留原有索引
create index idx_deleted
    on post_favorite (deleted);

create index idx_post_id
    on post_favorite (post_id);

create index idx_user_id
    on post_favorite (user_id);

CREATE TABLE `user_follows` (
                                `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                `follower_id` bigint NOT NULL COMMENT '关注者ID',
                                `following_id` bigint NOT NULL COMMENT '被关注者ID',
                                `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '关注时间',
                                PRIMARY KEY (`id`),
                                UNIQUE KEY `uk_follower_following` (`follower_id`, `following_id`) COMMENT '防止重复关注',
                                KEY `idx_follower_id` (`follower_id`) COMMENT '关注者索引',
                                KEY `idx_following_id` (`following_id`) COMMENT '被关注者索引',
                                CONSTRAINT `fk_user_follows_follower` FOREIGN KEY (`follower_id`)
                                    REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
                                CONSTRAINT `fk_user_follows_following` FOREIGN KEY (`following_id`)
                                    REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户关注关系表';

CREATE TABLE friendship (
                            user_id BIGINT NOT NULL,
                            friend_id BIGINT NOT NULL,
                            create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
                            update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                            deleted TINYINT DEFAULT 0,
                            PRIMARY KEY (user_id, friend_id)
);

CREATE TABLE friend_request (
                                id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                from_user_id BIGINT NOT NULL,
                                to_user_id BIGINT NOT NULL,
                                message VARCHAR(255),
                                status ENUM('pending', 'accepted', 'rejected', 'cancelled') DEFAULT 'pending',
                                create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
                                update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                UNIQUE (from_user_id, to_user_id)
);
