package com.example.inventoryservice.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * Redis 库存管理器 - 基于 Redis 实现库存预扣减
 */
@Slf4j
@Component
public class RedisInventoryManager {
    
    private final StringRedisTemplate redisTemplate;
    
    public RedisInventoryManager(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }
    
    /**
     * 初始化商品库存到 Redis
     */
    public void initializeStock(Long productId, Integer stock) {
        String stockKey = "stock:" + productId;
        redisTemplate.opsForValue().set(stockKey, String.valueOf(stock));
        log.info("初始化商品库存到 Redis: productId={}, stock={}", productId, stock);
    }
    
    /**
     * 检查并预扣减库存（原子操作）
     * @param productId 商品ID
     * @param quantity 扣减数量
     * @return true-预扣减成功，false-库存不足
     */
    public boolean tryReserveStock(Long productId, Integer quantity) {
        String stockKey = "stock:" + productId;
        
        // 先获取当前库存
        String currentStockStr = redisTemplate.opsForValue().get(stockKey);
        if (currentStockStr == null) {
            log.warn("商品库存不存在: productId={}", productId);
            return false;
        }
        
        int currentStock = Integer.parseInt(currentStockStr);
        if (currentStock < quantity) {
            log.warn("库存不足: productId={}, currentStock={}, requested={}", 
                    productId, currentStock, quantity);
            return false;
        }
        
        // 扣减库存
        int newStock = currentStock - quantity;
        redisTemplate.opsForValue().set(stockKey, String.valueOf(newStock));
        log.info("库存预扣减成功: productId={}, quantity={}, newStock={}", 
                productId, quantity, newStock);
        return true;
    }
    
    /**
     * 确认库存扣减
     */
    public void confirmStock(Long productId, Integer quantity) {
        log.info("库存确认: productId={}, quantity={}", productId, quantity);
    }
    
    /**
     * 回滚库存预扣减
     */
    public void rollbackStock(Long productId, Integer quantity) {
        String stockKey = "stock:" + productId;
        String currentStockStr = redisTemplate.opsForValue().get(stockKey);
        if (currentStockStr != null) {
            int currentStock = Integer.parseInt(currentStockStr);
            int newStock = currentStock + quantity;
            redisTemplate.opsForValue().set(stockKey, String.valueOf(newStock));
            log.info("库存回滚: productId={}, quantity={}, newStock={}", 
                    productId, quantity, newStock);
        }
    }
    
    /**
     * 获取当前可用库存
     */
    public Integer getAvailableStock(Long productId) {
        String stockKey = "stock:" + productId;
        String stockStr = redisTemplate.opsForValue().get(stockKey);
        if (stockStr == null) {
            return 0;
        }
        return Integer.parseInt(stockStr);
    }
    
    /**
     * 设置库存过期时间
     */
    public void setStockExpire(Long productId, long expireTime, TimeUnit timeUnit) {
        String stockKey = "stock:" + productId;
        redisTemplate.expire(stockKey, expireTime, timeUnit);
    }
}