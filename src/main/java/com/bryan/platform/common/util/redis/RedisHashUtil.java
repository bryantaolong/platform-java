package com.bryan.platform.common.util.redis;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * ClassName: RedisHashUtil
 * Package: com.bryan.platform.common.util.redis
 * Description: Redis Hash 类型操作工具类。
 * 优化了 RedisTemplate 的注入泛型以及 Hash 相关方法的泛型，
 * 使其与 RedisConfig 中配置的序列化器类型更加匹配，提升类型安全性。
 * Author: Bryan Long
 * Create: 2025/6/20 - 17:27
 * Version: v1.0
 */
@Slf4j
@Component
public class RedisHashUtil extends BaseRedisUtil {

    // 构造器注入 RedisTemplate<String, Object>，与 BaseRedisUtil 的泛型保持一致
    public RedisHashUtil(RedisTemplate<String, Object> redisTemplate) {
        super(redisTemplate);
    }

    /**
     * 在 Redis 的 Hash 中存储多个键值对。
     * 哈希的字段（hashKey）将被序列化为 String，值（hashValue）将被序列化为 JSON。
     *
     * @param key   哈希的键 (String)，不能为 null
     * @param value 哈希的键值对 (Map<String, Object>)，不能为 null
     * @return 操作成功返回 true，失败返回 false
     */
    public boolean set(String key, Map<String, Object> value) { // 优化：明确 Map 的泛型为 <String, Object>
        try {
            redisTemplate.opsForHash().putAll(key, value);
            return true;
        } catch (Exception e) {
            log.error("Redis hSetAll 操作失败，key: {}, value: {}", key, value, e);
            return false;
        }
    }

    /**
     * 在 Redis 的 Hash 中存储一个键值对。
     * 哈希的字段（hashKey）将被序列化为 String，值（value）将被序列化为 JSON。
     *
     * @param key     哈希的键 (String)，不能为 null
     * @param hashKey 哈希字段的键 (String)，不能为 null
     * @param value   哈希字段的值 (Object)，可以为任意对象
     * @return 操作成功返回 true，失败返回 false
     */
    public boolean set(String key, String hashKey, Object value) {
        try {
            redisTemplate.opsForHash().put(key, hashKey, value);
            return true;
        } catch (Exception e) {
            log.error("Redis hSet 操作失败，key: {}, hashKey: {}, value: {}", key, hashKey, value, e);
            return false;
        }
    }

    /**
     * 在 Redis 的 Hash 中获取某个字段的值。
     * 值将从 JSON 反序列化为 Object。
     *
     * @param key     哈希的键 (String)，不能为 null
     * @param hashKey 哈希字段的键 (String)，不能为 null
     * @return 哈希字段的值 (Object)，若字段不存在返回 null
     */
    public Object get(String key, String hashKey) {
        try {
            return redisTemplate.opsForHash().get(key, hashKey);
        } catch (Exception e) {
            log.error("Redis hGet 操作失败，key: {}, hashKey: {}", key, hashKey, e);
            return null;
        }
    }

    /**
     * 从 Redis 的 Hash 中删除一个字段。
     *
     * @param key     哈希的键 (String)，不能为 null
     * @param hashKey 哈希字段的键 (String)，不能为 null
     * @return 若字段删除成功返回 true，若字段不存在或删除失败返回 false
     */
    public boolean delete(String key, String hashKey) {
        try {
            // RedisTemplate 的 delete 方法返回 Long，需要检查是否大于 0 来判断是否成功删除
            return redisTemplate.opsForHash().delete(key, hashKey) > 0;
        } catch (Exception e) {
            log.error("Redis hDelete 操作失败，key: {}, hashKey: {}", key, hashKey, e);
            return false;
        }
    }

    /**
     * 检查 Redis 的 Hash 中是否存在某个字段。
     *
     * @param key     哈希的键 (String)，不能为 null
     * @param hashKey 哈希字段的键 (String)，不能为 null
     * @return 若字段存在返回 true，不存在返回 false
     */
    public boolean hasKey(String key, String hashKey) {
        try {
            return Boolean.TRUE.equals(redisTemplate.opsForHash().hasKey(key, hashKey));
        } catch (Exception e) {
            log.error("Redis hHasKey 操作失败，key: {}, hashKey: {}", key, hashKey, e);
            return false;
        }
    }

    /**
     * 获取 Redis 的 Hash 中所有字段和对应值。
     * 字段（keys）将是 String，值（values）将是 Object (JSON 反序列化后)。
     *
     * @param key 哈希的键 (String)，不能为 null
     * @return 哈希中的所有字段和值 (Map<String, Object>)，若键不存在返回空 Map
     */
    public Map<String, Object> getAll(String key) { // 优化：明确 Map 的泛型为 <String, Object>
        try {
            // opsForHash().entries() 返回的 Map 的 key 类型是 K（这里是 String），value 类型是 V（这里是 Object）
            return (Map<String, Object>) (Map<?, ?>) redisTemplate.opsForHash().entries(key);
        } catch (Exception e) {
            log.error("Redis hGetAll 操作失败，key: {}", key, e);
            return Collections.emptyMap();
        }
    }

    /**
     * 获取 Redis 的 Hash 中所有字段。
     * 字段将从 String 序列化器中获取。
     *
     * @param key 哈希的键 (String)，不能为 null
     * @return 哈希中的所有字段 (Set<String>)，若键不存在返回空 Set
     */
    public Set<String> keys(String key) { // 优化：明确 Set 的泛型为 <String>
        try {
            // opsForHash().keys() 返回的 Set 的元素类型是 HK（这里是 String）
            return (Set<String>) (Set<?>) redisTemplate.opsForHash().keys(key);
        } catch (Exception e) {
            log.error("Redis hKeys 操作失败，key: {}", key, e);
            return Collections.emptySet();
        }
    }

    /**
     * 获取 Redis 的 Hash 中所有字段的值。
     * 值将从 JSON 反序列化为 Object。
     *
     * @param key 哈希的键 (String)，不能为 null
     * @return 哈希中的所有字段的值 (List<Object>)，若键不存在返回空 List
     */
    public List<Object> values(String key) {
        try {
            return redisTemplate.opsForHash().values(key);
        } catch (Exception e) {
            log.error("Redis hValues 操作失败，key: {}", key, e);
            return Collections.emptyList();
        }
    }

    /**
     * 获取 Redis 的 Hash 中字段数量。
     *
     * @param key 哈希的键 (String)，不能为 null
     * @return 哈希中的字段数量，若键不存在返回 0
     */
    public long size(String key) {
        try {
            Long count = redisTemplate.opsForHash().size(key);
            return count != null ? count : 0L;
        } catch (Exception e) {
            log.error("Redis hSize 操作失败，key: {}", key, e);
            return 0L;
        }
    }
}
