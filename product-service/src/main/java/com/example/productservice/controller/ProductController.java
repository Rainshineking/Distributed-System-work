package com.example.productservice.controller;

import com.example.common.core.Result;
import com.example.productservice.dto.CreateProductRequest;
import com.example.productservice.entity.Product;
import com.example.productservice.entity.ProductDocument;
import com.example.productservice.service.ProductSearchService;
import com.example.productservice.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {
    
    private final ProductService productService;
    private final ProductSearchService productSearchService;
    
    @GetMapping("/health")
    public Result<String> health() {
        return Result.success("Product Service is running");
    }
    
    @GetMapping("/status")
    public Result<String> status() {
        return Result.success("Product Service status: OK");
    }
    
    @PostMapping
    public Result<Product> createProduct(@Valid @RequestBody CreateProductRequest request) {
        Product product = productService.createProduct(request);
        return Result.success("商品创建成功", product);
    }
    
    @GetMapping("/{id}")
    public Result<Product> getProductById(@PathVariable("id") Long id) {
        Product product = productService.getProductById(id);
        return Result.success(product);
    }
    
    @GetMapping
    public Result<List<Product>> getAllProducts() {
        List<Product> products = productService.getAllProducts();
        return Result.success(products);
    }
    
    @PutMapping("/{id}")
    public Result<Product> updateProduct(@PathVariable("id") Long id, @Valid @RequestBody CreateProductRequest request) {
        Product product = productService.updateProduct(id, request);
        return Result.success("商品更新成功", product);
    }
    
    @DeleteMapping("/{id}")
    public Result<String> deleteProduct(@PathVariable("id") Long id) {
        productService.deleteProduct(id);
        return Result.success("商品删除成功");
    }
    
    /**
     * 搜索商品（ElasticSearch）
     */
    @GetMapping("/search")
    public Result<Page<ProductDocument>> searchProducts(
            @RequestParam("keyword") String keyword,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        Page<ProductDocument> result = productSearchService.searchByName(keyword, page, size);
        return Result.success(result);
    }
    
    /**
     * 同步所有商品到 ElasticSearch
     */
    @PostMapping("/sync/all")
    public Result<String> syncAllProducts() {
        productSearchService.syncAllProducts();
        return Result.success("已同步所有商品到 ElasticSearch");
    }
}
