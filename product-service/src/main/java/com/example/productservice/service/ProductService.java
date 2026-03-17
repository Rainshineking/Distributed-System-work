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

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {
    
    private final ProductRepository productRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    
    @Transactional
    public Product createProduct(CreateProductRequest request) {
        if (productRepository.existsByName(request.getName())) {
            throw new BusinessException("商品名称已存在");
        }
        
        Product product = new Product();
        product.setName(request.getName());
        product.setPrice(request.getPrice());
        product.setDescription(request.getDescription());
        product.setImageUrl(request.getImageUrl());
        
        productRepository.save(product);
        log.info("商品创建成功：{}", product.getName());
        
        return product;
    }
    
    @Transactional(readOnly = true)
    public Product getProductById(Long id) {
        String cacheKey = "product:" + id;
        
        Product product = (Product) redisTemplate.opsForValue().get(cacheKey);
        if (product != null) {
            log.info("从缓存获取商品：{}", id);
            return product;
        }
        
        product = productRepository.findById(id)
                .orElseThrow(() -> new BusinessException("商品不存在"));
        
        redisTemplate.opsForValue().set(cacheKey, product, 30, TimeUnit.MINUTES);
        log.info("从数据库获取商品并缓存：{}", id);
        
        return product;
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
        
        String cacheKey = "product:" + id;
        redisTemplate.delete(cacheKey);
        log.info("商品更新成功：{}", product.getName());
        
        return product;
    }
    
    @Transactional
    public void deleteProduct(Long id) {
        getProductById(id);
        productRepository.deleteById(id);
        
        String cacheKey = "product:" + id;
        redisTemplate.delete(cacheKey);
        log.info("商品删除成功：{}", id);
    }
}
