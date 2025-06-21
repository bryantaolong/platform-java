INSERT INTO `user` (
    `username`,
    `password`,
    `email`,
    `status`,
    `deleted`,
    `create_time`,
    `update_time`
) VALUES (
             'Bryan Long',
             '$2a$10$xJwL5vWZ5D2bBQZQ5X5XeO9XQ5XeO9XQ5XeO9XQ5XeO9XQ5XeO9XQ5X', -- BCrypt加密后的密码（明文如123456）
             'test@example.com',
             0, -- 状态：0-正常
             0, -- 逻辑删除：0-未删除
             NOW(), -- 创建时间（自动填充）
             NOW()  -- 更新时间（自动填充）
         );
