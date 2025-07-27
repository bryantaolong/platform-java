create sequence "user_id_seq";

create table "user"
(
    id                  bigint  default nextval('user_id_seq'::regclass) not null primary key,
    username            varchar(255)                                     not null,
    password            varchar(255)                                     not null,
    phone_number        varchar(50),
    email               varchar(255),
    gender              integer,
    avatar              varchar(512),
    status              integer default 0,
    roles               varchar(255),
    login_time          timestamp,
    login_ip            varchar(255),
    password_reset_time timestamp,
    deleted             integer default 0,
    create_time         timestamp                                        not null,
    create_by           varchar(255),
    update_time         timestamp,
    update_by           varchar(255)
);

comment on table "user" is '用户表，存储系统用户的基本信息、认证信息和状态';

comment on column "user".id is '用户ID，主键，自增长';
comment on column "user".username is '用户名，用于登录的唯一标识';
comment on column "user".password is '加密后的用户密码';
comment on column "user".phone_number is '用户手机号码';
comment on column "user".email is '用户电子邮箱';
comment on column "user".gender is '用户性别(0-未知 1-男 2-女)';
comment on column "user".avatar is '用户头像URL地址';
comment on column "user".status is '用户状态(0-正常 1-禁用)';
comment on column "user".roles is '用户角色，多个角色用逗号分隔';
comment on column "user".login_time is '最后一次登录时间';
comment on column "user".login_ip is '最后一次登录IP地址';
comment on column "user".password_reset_time is '密码重置时间';
comment on column "user".deleted is '软删除标记(0-未删除 1-已删除)';
comment on column "user".create_time is '记录创建时间';
comment on column "user".create_by is '记录创建人';
comment on column "user".update_time is '记录更新时间';
comment on column "user".update_by is '记录更新人';

create index idx_user_username
    on "user" (username);

comment on index idx_user_username is '用户名索引，用于加速用户名查询';


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

