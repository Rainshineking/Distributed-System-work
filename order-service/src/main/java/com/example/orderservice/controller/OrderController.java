package com.example.orderservice.controller;

import com.example.common.core.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 订单控制器
 */
@RestController
@RequestMapping("/api/orders")
public class OrderController {
    
    @GetMapping("/health")
    public Result<String> health() {
        return Result.success("Order Service is running");
    }
    
    @GetMapping("/status")
    public Result<String> status() {
        return Result.success("Order Service status: OK");
    }
}
