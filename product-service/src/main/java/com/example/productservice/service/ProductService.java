package com.example.productservice.service;

import com.example.common.exception.BusinessException;
import com.example.productservice.dto.CreateProductRequest;
import com.example.productservice.entity.Product;
import com.example.productservice.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {
    
    private final ProductRepository productRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ReentrantLock cacheLock = new ReentrantLock();
    private final ProductSearchService searchService;
    
    // 缓存穿透防护：缓存空对象的过期时间（5 分钟）
    private static final int CACHE_NULL_TTL = 5;
    
    // 缓存雪崩防护：基础过期时间 30 分钟，随机波动±5 分钟
    private static final int CACHE_BASE_TTL = 30;
    private static final int CACHE_RANDOM_TTL = 10;
    
    @Transactional
    public Product createProduct(CreateProductRequest request) {
        if (productRepository.existsByName(request.getName())) {
            throw new BusinessException("已创建");
        }
        
        Product product = new Product();
        product.setName(request.getName());
        product.setPrice(request.getPrice());
        product.setDescription(request.getDescription());
        product.setImageUrl(request.getImageUrl());
        
        productRepository.save(product);
        log.info("商品创建成功：{}", product.getName());
        
        // 同步到 ElasticSearch
        try {
            searchService.saveToElasticsearch(product.getId());
        } catch (Exception e) {
            log.error("同步商品到 ElasticSearch 失败：{}", e.getMessage());
        }
        
        return product;
    }
    
    @Transactional(readOnly = true)
    public Product getProductById(Long id) {
        String cacheKey = "product:" + id;
        
        // 缓存穿透防护：先查缓存
        Product product = (Product) redisTemplate.opsForValue().get(cacheKey);
        if (product != null) {
            log.info("从缓存获取商品：{}", id);
            return product;
        }
        
        // 缓存穿透防护：如果缓存中是空值，说明之前查询过但不存在
        if (Boolean.FALSE.equals(redisTemplate.hasKey(cacheKey))) {
            log.warn("缓存穿透防护：商品 {} 不存在（空值缓存）", id);
            throw new BusinessException("商品不存在");
        }
        
        // 缓存击穿防护：使用互斥锁重建缓存
        cacheLock.lock();
        try {
            // 双重检查缓存（防止其他线程已重建缓存）
            product = (Product) redisTemplate.opsForValue().get(cacheKey);
            if (product != null) {
                log.info("缓存击穿防护：从缓存获取商品（互斥锁）：{}", id);
                return product;
            }
            
            // 查询数据库
            product = productRepository.findById(id)
                    .orElseThrow(() -> new BusinessException("商品不存在"));
            
            // 缓存雪崩防护：设置随机过期时间
            int randomTTL = CACHE_BASE_TTL + (int)(Math.random() * CACHE_RANDOM_TTL) - CACHE_RANDOM_TTL / 2;
            redisTemplate.opsForValue().set(cacheKey, product, randomTTL, TimeUnit.MINUTES);
            log.info("从数据库获取商品并缓存（随机 TTL={} 分钟）：{}", randomTTL, id);
            
            return product;
        } finally {
            cacheLock.unlock();
        }
    }
    
    @Transactional(readOnly = true)
    public List<Product> getAllProducts() {
        return productRepository.findByStatus(1);
    }
    
    @Transactional
    public Product updateProduct(Long id, CreateProductRequest request) {
        Product product = getProductById(id);
        product.setName(request.getName());
        product.setPrice(request.getPrice());
        product.setDescription(request.getDescription());
        product.setImageUrl(request.getImageUrl());
        
        productRepository.save(product);
        
        // 缓存更新：删除旧缓存
        String cacheKey = "product:" + id;
        redisTemplate.delete(cacheKey);
        log.info("商品更新成功并删除缓存：{}", product.getName());
        
        // 同步到 ElasticSearch
        try {
            searchService.saveToElasticsearch(product.getId());
        } catch (Exception e) {
            log.error("同步商品到 ElasticSearch 失败：{}", e.getMessage());
        }
        
        return product;
    }
    
    @Transactional
    public void deleteProduct(Long id) {
        // 先获取商品（会触发缓存）
        getProductById(id);
        productRepository.deleteById(id);
        
        // 删除缓存
        String cacheKey = "product:" + id;
        redisTemplate.delete(cacheKey);
        log.info("商品删除成功并删除缓存：{}", id);
        
        // 从 ElasticSearch 删除
        try {
            searchService.deleteFromElasticsearch(id);
        } catch (Exception e) {
            log.error("从 ElasticSearch 删除商品失败：{}", e.getMessage());
        }
    }
}
