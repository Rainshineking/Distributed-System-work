package com.example.productservice.service;

import com.example.productservice.entity.Product;
import com.example.productservice.entity.ProductDocument;
import com.example.productservice.repository.ProductRepository;
import com.example.productservice.repository.ProductSearchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * 商品搜索服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductSearchService {
    
    private final ProductSearchRepository searchRepository;
    private final ProductRepository productRepository;
    
    /**
     * 保存商品到 ElasticSearch
     */
    public ProductDocument saveToElasticsearch(Long productId) {
        Optional<Product> productOpt = productRepository.findById(productId);
        if (productOpt.isPresent()) {
            Product product = productOpt.get();
            ProductDocument document = new ProductDocument();
            document.setId(product.getId());
            document.setName(product.getName());
            document.setPrice(product.getPrice());
            document.setDescription(product.getDescription());
            document.setStatus(product.getStatus());
            
            searchRepository.save(document);
            log.info("商品已同步到 ElasticSearch: {}", productId);
            return document;
        }
        return null;
    }
    
    /**
     * 从 ElasticSearch 删除商品
     */
    public void deleteFromElasticsearch(Long productId) {
        searchRepository.deleteById(productId);
        log.info("商品已从 ElasticSearch 删除：{}", productId);
    }
    
    /**
     * 搜索商品（按名称）
     */
    public Page<ProductDocument> searchByName(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        log.info("搜索商品：keyword={}, page={}, size={}", keyword, page, size);
        return searchRepository.findByNameContaining(keyword, pageable);
    }
    
    /**
     * 搜索商品（按描述）
     */
    public Page<ProductDocument> searchByDescription(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        log.info("搜索商品描述：keyword={}, page={}, size={}", keyword, page, size);
        return searchRepository.findByDescriptionContaining(keyword, pageable);
    }
    
    /**
     * 同步所有商品到 ElasticSearch
     */
    public void syncAllProducts() {
        Iterable<Product> products = productRepository.findAll();
        int count = 0;
        for (Product product : products) {
            ProductDocument document = new ProductDocument();
            document.setId(product.getId());
            document.setName(product.getName());
            document.setPrice(product.getPrice());
            document.setDescription(product.getDescription());
            document.setStatus(product.getStatus());
            
            searchRepository.save(document);
            count++;
        }
        log.info("已同步 {} 个商品到 ElasticSearch", count);
    }
}
