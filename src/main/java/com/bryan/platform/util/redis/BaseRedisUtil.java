package com.bryan.platform.util.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * Redis 工具类的抽象基类，提供 RedisTemplate 的通用注入。
 * 将 RedisTemplate 的泛型明确为 <String, Object>，与 RedisConfig 配置保持一致。
 *
 * @author Bryan Long
 * @since 2025/6/20
 * @version 1.0
 */
@RequiredArgsConstructor
public abstract class BaseRedisUtil {
    // 明确 RedisTemplate 的泛型为 <String, Object>
    // 对应 RedisConfig 中 KeySerializer 为 String，ValueSerializer 为 JSON (Object)
    protected final RedisTemplate<String, Object> redisTemplate;
}
