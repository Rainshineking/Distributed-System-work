package com.example.productservice.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import com.example.productservice.entity.ProductDocument;

/**
 * ElasticSearch 商品 Repository
 */
@Repository
public interface ProductSearchRepository extends ElasticsearchRepository<ProductDocument, Long> {
    
    /**
     * 按名称搜索商品
     */
    Page<ProductDocument> findByNameContaining(String name, Pageable pageable);
    
    /**
     * 按描述搜索商品
     */
    Page<ProductDocument> findByDescriptionContaining(String description, Pageable pageable);
}
