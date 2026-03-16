package com.example.productservice.controller;

import com.example.common.core.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 商品控制器
 */
@RestController
@RequestMapping("/api/products")
public class ProductController {
    
    @GetMapping("/health")
    public Result<String> health() {
        return Result.success("Product Service is running");
    }
    
    @GetMapping("/status")
    public Result<String> status() {
        return Result.success("Product Service status: OK");
    }
}
