package com.bryan.platform.service.redis;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * Redis 字符串 (String) 类型操作工具类。
 * 优化了 RedisTemplate 的注入泛型，使其与 RedisConfig 中配置的序列化器类型匹配。
 *
 * @author Bryan Long
 * @version 1.0
 * @since 2025/6/20
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RedisStringService {

    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 在 Redis 中存储一个键值对（无过期时间）。
     * 键将使用 String 序列化器，值将使用 JSON 序列化器。
     *
     * @param key   键 (String)，不能为 null
     * @param value 值 (Object)，可以为任意对象
     * @return 操作成功返回 true，失败返回 false
     */
    public boolean set(String key, Object value) {
        try {
            redisTemplate.opsForValue().set(key, value);
            return true;
        } catch (Exception e) {
            log.error("Redis set 操作失败，key: {}, value: {}", key, value, e);
            return false;
        }
    }

    /**
     * 在 Redis 中存储一个带有过期时间的键值对。
     * 键将使用 String 序列化器，值将使用 JSON 序列化器。
     *
     * @param key     键 (String)，不能为 null
     * @param value   值 (Object)，可以为任意对象
     * @param seconds 过期时间（秒），必须大于 0
     * @return 操作成功返回 true，失败返回 false
     */
    public boolean set(String key, Object value, long seconds) {
        try {
            if (seconds > 0) { // Ensure expiration time is valid
                redisTemplate.opsForValue().set(key, value, Duration.ofSeconds(seconds));
                return true;
            } else {
                log.warn("Redis setWithExpire 操作：过期时间必须大于0，key: {}", key);
                return set(key, value); // Fallback to set without expiration if seconds <= 0
            }
        } catch (Exception e) {
            log.error("Redis setWithExpire 操作失败，key: {}, value: {}, seconds: {}", key, value, seconds, e);
            return false;
        }
    }

    /**
     * 为 Redis 中已存在的键设置过期时间。
     *
     * @param key     键 (String)，不能为 null
     * @param seconds 过期时间（秒），必须大于 0
     * @return 操作成功返回 true，若键不存在或设置失败返回 false
     */
    public boolean setExpire(String key, long seconds) {
        try {
            if (seconds <= 0) {
                log.warn("Redis setExpire 操作：过期时间必须大于0，key: {}", key);
                return false; // Return false if expiration is not valid
            }
            return Boolean.TRUE.equals(redisTemplate.expire(key, Duration.ofSeconds(seconds)));
        } catch (Exception e) {
            log.error("Redis setExpire 操作失败，key: {}, seconds: {}", key, seconds, e);
            return false;
        }
    }

    /**
     * 从 Redis 中获取与键对应的值。
     * 值将从 JSON 反序列化为 Object。
     *
     * @param key 键 (String)，不能为 null
     * @return 键对应的值 (Object)，若键不存在返回 null
     */
    public Object get(String key) {
        try {
            return redisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            log.error("Redis get 操作失败，key: {}", key, e);
            return null;
        }
    }

    /**
     * 从 Redis 中删除一个键。
     *
     * @param key 键 (String)，不能为 null
     * @return 若键删除成功返回 true，若键不存在返回 false
     */
    public boolean delete(String key) {
        try {
            return Boolean.TRUE.equals(redisTemplate.delete(key));
        } catch (Exception e) {
            log.error("Redis delete 操作失败，key: {}", key, e);
            return false;
        }
    }

    /**
     * 检查 Redis 中是否存在某个键。
     *
     * @param key 键 (String)，不能为 null
     * @return 若键存在返回 true，不存在返回 false
     */
    public boolean hasKey(String key) {
        try {
            return Boolean.TRUE.equals(redisTemplate.hasKey(key));
        } catch (Exception e) {
            log.error("Redis hasKey 操作失败，key: {}", key, e);
            return false;
        }
    }

    /**
     * 对存储在指定键上的字符串值执行原子递增操作。
     * 如果键不存在，则在执行递增操作之前将其值设置为 0。
     *
     * @param key   键 (String)，不能为 null
     * @param delta 递增值 (long)
     * @return 递增后的新值
     */
    public Long increment(String key, long delta) {
        try {
            return redisTemplate.opsForValue().increment(key, delta);
        } catch (Exception e) {
            log.error("Redis increment 操作失败，key: {}, delta: {}", key, delta, e);
            return null;
        }
    }

    /**
     * 对存储在指定键上的字符串值执行原子递减操作。
     * 如果键不存在，则在执行递减操作之前将其值设置为 0。
     *
     * @param key   键 (String)，不能为 null
     * @param delta 递减值 (long)
     * @return 递减后的新值
     */
    public Long decrement(String key, long delta) {
        try {
            return redisTemplate.opsForValue().decrement(key, delta);
        } catch (Exception e) {
            log.error("Redis decrement 操作失败，key: {}, delta: {}", key, delta, e);
            return null;
        }
    }
}
