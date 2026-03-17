package com.example.inventoryservice.service;

import com.example.common.exception.BusinessException;
import com.example.inventoryservice.dto.DecreaseStockRequest;
import com.example.inventoryservice.entity.Inventory;
import com.example.inventoryservice.repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryService {
    
    private final InventoryRepository inventoryRepository;
    private final RedissonClient redissonClient;
    private final RedisTemplate<String, Object> redisTemplate;
    
    @Transactional
    public Inventory createInventory(Long productId, Integer stock) {
        Inventory inventory = new Inventory();
        inventory.setProductId(productId);
        inventory.setStock(stock);
        
        String cacheKey = "inventory:" + productId;
        redisTemplate.opsForValue().set(cacheKey, inventory, 30, TimeUnit.MINUTES);
        
        inventoryRepository.save(inventory);
        log.info("库存创建成功，商品 ID: {}, 库存：{}", productId, stock);
        
        return inventory;
    }
    
    @Transactional(readOnly = true)
    public Inventory getInventory(Long productId) {
        String cacheKey = "inventory:" + productId;
        Inventory inventory = (Inventory) redisTemplate.opsForValue().get(cacheKey);
        
        if (inventory != null) {
            return inventory;
        }
        
        inventory = inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new BusinessException("库存信息不存在"));
        
        redisTemplate.opsForValue().set(cacheKey, inventory, 30, TimeUnit.MINUTES);
        return inventory;
    }
    
    @Transactional
    public boolean decreaseStock(DecreaseStockRequest request) {
        Long productId = request.getProductId();
        Integer quantity = request.getQuantity();
        
        String lockKey = "lock:inventory:" + productId;
        RLock lock = redissonClient.getLock(lockKey);
        
        try {
            boolean isLocked = lock.tryLock(5, 10, TimeUnit.SECONDS);
            if (!isLocked) {
                throw new BusinessException("获取锁失败，系统繁忙");
            }
            
            Inventory inventory = getInventory(productId);
            
            if (inventory.getStock() < quantity) {
                throw new BusinessException("库存不足");
            }
            
            if (inventory.getStock() - inventory.getLockedStock() < quantity) {
                throw new BusinessException("可用库存不足");
            }
            
            inventory.setStock(inventory.getStock() - quantity);
            inventory.setLockedStock(inventory.getLockedStock() + quantity);
            
            inventoryRepository.save(inventory);
            
            String cacheKey = "inventory:" + productId;
            redisTemplate.delete(cacheKey);
            
            log.info("库存扣减成功，商品 ID: {}, 扣减数量：{}, 剩余库存：{}", 
                    productId, quantity, inventory.getStock());
            
            return true;
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException("获取锁中断");
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
    
    @Transactional
    public boolean confirmStock(DecreaseStockRequest request) {
        Long productId = request.getProductId();
        Integer quantity = request.getQuantity();
        
        Inventory inventory = getInventory(productId);
        inventory.setLockedStock(inventory.getLockedStock() - quantity);
        
        inventoryRepository.save(inventory);
        
        String cacheKey = "inventory:" + productId;
        redisTemplate.delete(cacheKey);
        
        log.info("库存确认成功，商品 ID: {}, 确认数量：{}", productId, quantity);
        
        return true;
    }
    
    @Transactional
    public boolean rollbackStock(DecreaseStockRequest request) {
        Long productId = request.getProductId();
        Integer quantity = request.getQuantity();
        
        Inventory inventory = getInventory(productId);
        inventory.setStock(inventory.getStock() + quantity);
        inventory.setLockedStock(inventory.getLockedStock() - quantity);
        
        inventoryRepository.save(inventory);
        
        String cacheKey = "inventory:" + productId;
        redisTemplate.delete(cacheKey);
        
        log.info("库存回滚成功，商品 ID: {}, 回滚数量：{}", productId, quantity);
        
        return true;
    }
}
