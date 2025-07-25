CREATE TABLE "user" (
                        id BIGSERIAL PRIMARY KEY,
                        username VARCHAR(50) NOT NULL,
                        password VARCHAR(100) NOT NULL,
                        email VARCHAR(100),
                        status SMALLINT DEFAULT 0,
                        deleted SMALLINT DEFAULT 0,
                        create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        roles VARCHAR(255) DEFAULT 'ROLE_USER',
                        CONSTRAINT idx_username UNIQUE (username)
);

COMMENT ON TABLE "user" IS '用户表';
COMMENT ON COLUMN "user".id IS '用户ID';
COMMENT ON COLUMN "user".username IS '用户名';
COMMENT ON COLUMN "user".password IS '加密后的密码';
COMMENT ON COLUMN "user".email IS '邮箱';
COMMENT ON COLUMN "user".status IS '状态（0-正常，1-封禁）';
COMMENT ON COLUMN "user".deleted IS '逻辑删除标记（0-未删除，1-已删除）';
COMMENT ON COLUMN "user".create_time IS '创建时间';
COMMENT ON COLUMN "user".update_time IS '更新时间';
COMMENT ON COLUMN "user".roles IS '用户角色，多个角色用逗号分隔';

CREATE INDEX idx_deleted ON "user" (deleted);

CREATE TABLE post_favorite (
                               user_id BIGINT NOT NULL,
                               post_id VARCHAR(64) NOT NULL,
                               deleted SMALLINT DEFAULT 0,
                               create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                               update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                               PRIMARY KEY (user_id, post_id)
);

COMMENT ON TABLE post_favorite IS '博文收藏记录表';
COMMENT ON COLUMN post_favorite.user_id IS '用户ID';
COMMENT ON COLUMN post_favorite.post_id IS '博文ID (MongoDB ObjectId)';
COMMENT ON COLUMN post_favorite.deleted IS '逻辑删除: 0-未删除, 1-已删除';
COMMENT ON COLUMN post_favorite.create_time IS '创建时间';
COMMENT ON COLUMN post_favorite.update_time IS '更新时间';

CREATE INDEX idx_deleted ON post_favorite (deleted);
CREATE INDEX idx_post_id ON post_favorite (post_id);
CREATE INDEX idx_user_id ON post_favorite (user_id);

CREATE TABLE user_follows (
                              id BIGSERIAL PRIMARY KEY,
                              follower_id BIGINT NOT NULL,
                              following_id BIGINT NOT NULL,
                              create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
                              CONSTRAINT uk_follower_following UNIQUE (follower_id, following_id)
);

COMMENT ON TABLE user_follows IS '用户关注关系表';
COMMENT ON COLUMN user_follows.id IS '主键ID';
COMMENT ON COLUMN user_follows.follower_id IS '关注者ID';
COMMENT ON COLUMN user_follows.following_id IS '被关注者ID';
COMMENT ON COLUMN user_follows.create_time IS '关注时间';

-- 添加外键约束
ALTER TABLE user_follows ADD CONSTRAINT fk_user_follows_follower
    FOREIGN KEY (follower_id) REFERENCES "user" (id) ON UPDATE CASCADE ON DELETE CASCADE;

ALTER TABLE user_follows ADD CONSTRAINT fk_user_follows_following
    FOREIGN KEY (following_id) REFERENCES "user" (id) ON UPDATE CASCADE ON DELETE CASCADE;

-- 创建索引
CREATE INDEX idx_follower_id ON user_follows (follower_id);
CREATE INDEX idx_following_id ON user_follows (following_id);

