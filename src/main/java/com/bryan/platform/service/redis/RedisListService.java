package com.bryan.platform.service.redis;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Collections; // Added import for Collections.emptyList()
import java.util.List;

/**
 * Redis 列表 (List) 类型操作工具类。
 * 优化了 RedisTemplate 的注入泛型，使其与 RedisConfig 中配置的序列化器类型匹配。
 *
 * @author Bryan Long
 * @version 1.0
 * @since 2025/6/20
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RedisListService {

    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 向 Redis 列表左侧添加一个或多个元素。
     * 元素将使用 JSON 序列化器进行序列化。
     *
     * @param key   列表键 (String)，不能为 null
     * @param value 要添加的值 (Object)
     * @return 操作成功返回 true，失败返回 false
     */
    public boolean leftPush(String key, Object value) {
        try {
            redisTemplate.opsForList().leftPush(key, value);
            return true;
        } catch (Exception e) {
            log.error("Redis leftPush 操作失败，key: {}, value: {}", key, value, e);
            return false;
        }
    }

    /**
     * 向 Redis 列表左侧添加一个或多个元素。
     *
     * @param key    列表键 (String)，不能为 null
     * @param values 要添加的多个值 (Object...)
     * @return 操作成功返回 true，失败返回 false
     */
    public boolean leftPushAll(String key, Object... values) {
        try {
            redisTemplate.opsForList().leftPushAll(key, values);
            return true;
        } catch (Exception e) {
            log.error("Redis leftPushAll 操作失败，key: {}, values: {}", key, values, e);
            return false;
        }
    }

    /**
     * 向 Redis 列表右侧添加一个或多个元素。
     * 元素将使用 JSON 序列化器进行序列化。
     *
     * @param key   列表键 (String)，不能为 null
     * @param value 要添加的值 (Object)
     * @return 操作成功返回 true，失败返回 false
     */
    public boolean rightPush(String key, Object value) {
        try {
            redisTemplate.opsForList().rightPush(key, value);
            return true;
        } catch (Exception e) {
            log.error("Redis rightPush 操作失败，key: {}, value: {}", key, value, e);
            return false;
        }
    }

    /**
     * 向 Redis 列表右侧添加一个或多个元素。
     *
     * @param key    列表键 (String)，不能为 null
     * @param values 要添加的多个值 (Object...)
     * @return 操作成功返回 true，失败返回 false
     */
    public boolean rightPushAll(String key, Object... values) {
        try {
            redisTemplate.opsForList().rightPushAll(key, values);
            return true;
        } catch (Exception e) {
            log.error("Redis rightPushAll 操作失败，key: {}, values: {}", key, values, e);
            return false;
        }
    }

    /**
     * 从 Redis 列表左侧弹出元素。
     * 元素将从 JSON 反序列化为 Object。
     *
     * @param key 列表键 (String)，不能为 null
     * @return 弹出的元素 (Object)，若列表为空或键不存在返回 null
     */
    public Object leftPop(String key) {
        try {
            return redisTemplate.opsForList().leftPop(key);
        } catch (Exception e) {
            log.error("Redis leftPop 操作失败，key: {}", key, e);
            return null;
        }
    }

    /**
     * 从 Redis 列表右侧弹出元素。
     * 元素将从 JSON 反序列化为 Object。
     *
     * @param key 列表键 (String)，不能为 null
     * @return 弹出的元素 (Object)，若列表为空或键不存在返回 null
     */
    public Object rightPop(String key) {
        try {
            return redisTemplate.opsForList().rightPop(key);
        } catch (Exception e) {
            log.error("Redis rightPop 操作失败，key: {}", key, e);
            return null;
        }
    }

    /**
     * 获取 Redis 列表中指定范围的元素。
     *
     * @param key   列表键 (String)，不能为 null
     * @param start 起始索引（包含），0 表示第一个元素，-1 表示最后一个元素
     * @param end   结束索引（包含），-1 表示最后一个元素
     * @return 列表中的元素 (List<Object>)，若键不存在返回空列表
     */
    public List<Object> range(String key, long start, long end) {
        try {
            List<Object> result = redisTemplate.opsForList().range(key, start, end);
            return result != null ? result : Collections.emptyList(); // Handle null case
        } catch (Exception e) {
            log.error("Redis range 操作失败，key: {}, start: {}, end: {}", key, start, end, e);
            return Collections.emptyList(); // Return empty list on error
        }
    }

    /**
     * 获取 Redis 列表的长度。
     *
     * @param key 列表键 (String)，不能为 null
     * @return 列表长度 (long)，若键不存在返回 0
     */
    public long size(String key) {
        try {
            Long size = redisTemplate.opsForList().size(key);
            return size != null ? size : 0L; // Ensure 0L for long return type
        } catch (Exception e) {
            log.error("Redis size 操作失败，key: {}", key, e);
            return 0L;
        }
    }

    /**
     * 根据索引从 Redis 列表中获取元素。
     *
     * @param key   列表键 (String)，不能为 null
     * @param index 元素索引 (long)，0 表示第一个元素，负数表示从尾部开始计数 (-1 为最后一个)
     * @return 索引对应的元素 (Object)，若键不存在或索引超出范围返回 null
     */
    public Object index(String key, long index) {
        try {
            return redisTemplate.opsForList().index(key, index);
        } catch (Exception e) {
            log.error("Redis index 操作失败，key: {}, index: {}", key, index, e);
            return null;
        }
    }

    /**
     * 根据索引更新 Redis 列表中的元素。
     *
     * @param key   列表键 (String)，不能为 null
     * @param index 元素索引 (long)
     * @param value 新值 (Object)
     * @return 操作成功返回 true，失败返回 false
     */
    public boolean set(String key, long index, Object value) {
        try {
            redisTemplate.opsForList().set(key, index, value);
            return true;
        } catch (Exception e) {
            log.error("Redis set (by index) 操作失败，key: {}, index: {}, value: {}", key, index, value, e);
            return false;
        }
    }

    /**
     * 删除 Redis 列表中指定数量的元素。
     *
     * @param key   列表键 (String)，不能为 null
     * @param count 要删除的元素数量 (long)。
     * count > 0: 从表头开始向表尾搜索，移除与 value 相等的元素，数量为 count。
     * count < 0: 从表尾开始向表头搜索，移除与 value 相等的元素，数量为 count 的绝对值。
     * count = 0: 移除所有与 value 相等的元素。
     * @param value 要删除的值 (Object)
     * @return 删除的元素数量 (long)，若键不存在返回 0
     */
    public long remove(String key, long count, Object value) {
        try {
            Long removedCount = redisTemplate.opsForList().remove(key, count, value);
            return removedCount != null ? removedCount : 0L; // Ensure 0L for long return type
        } catch (Exception e) {
            log.error("Redis remove 操作失败，key: {}, count: {}, value: {}", key, count, value, e);
            return 0L;
        }
    }

    /**
     * 对一个列表进行修剪(trim)，只保留指定区间的元素。
     *
     * @param key   列表键 (String)，不能为 null
     * @param start 起始索引（包含）
     * @param end   结束索引（包含）
     * @return 操作成功返回 true，失败返回 false
     */
    public boolean trim(String key, long start, long end) {
        try {
            redisTemplate.opsForList().trim(key, start, end);
            return true;
        } catch (Exception e) {
            log.error("Redis trim 操作失败，key: {}, start: {}, end: {}", key, start, end, e);
            return false;
        }
    }
}
