package com.example.orderservice.util;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 幂等性控制工具 - 防止重复下单
 */
@Component
public class IdempotencyUtil {
    
    private final StringRedisTemplate redisTemplate;
    
    public IdempotencyUtil(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }
    
    /**
     * 检查并设置幂等性锁
     * @param key 唯一键（如 userId:productId）
     * @param value 值（如 orderId）
     * @param expireTime 过期时间
     * @param timeUnit 时间单位
     * @return true-设置成功（首次请求），false-已存在（重复请求）
     */
    public boolean trySetIfAbsent(String key, String value, long expireTime, TimeUnit timeUnit) {
        Boolean result = redisTemplate.opsForValue()
                .setIfAbsent(key, value, expireTime, timeUnit);
        return result != null && result;
    }
    
    /**
     * 检查是否存在幂等性锁
     * @param key 唯一键
     * @return true-存在（已下单），false-不存在（可下单）
     */
    public boolean exists(String key) {
        Boolean result = redisTemplate.hasKey(key);
        return result != null && result;
    }
    
    /**
     * 删除幂等性锁
     * @param key 唯一键
     */
    public void delete(String key) {
        redisTemplate.delete(key);
    }
}
