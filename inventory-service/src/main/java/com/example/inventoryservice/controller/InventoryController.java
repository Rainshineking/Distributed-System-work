package com.example.inventoryservice.controller;

import com.example.common.core.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 库存控制器
 */
@RestController
@RequestMapping("/api/inventory")
public class InventoryController {
    
    @GetMapping("/health")
    public Result<String> health() {
        return Result.success("Inventory Service is running");
    }
    
    @GetMapping("/status")
    public Result<String> status() {
        return Result.success("Inventory Service status: OK");
    }
}
