package com.example.inventoryservice.controller;

import com.example.common.core.Result;
import com.example.inventoryservice.dto.DecreaseStockRequest;
import com.example.inventoryservice.entity.Inventory;
import com.example.inventoryservice.service.InventoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {
    
    private final InventoryService inventoryService;
    
    @GetMapping("/health")
    public Result<String> health() {
        return Result.success("Inventory Service is running");
    }
    
    @GetMapping("/status")
    public Result<String> status() {
        return Result.success("Inventory Service status: OK");
    }
    
    @PostMapping("/decrease")
    public Result<Boolean> decreaseStock(@Valid @RequestBody DecreaseStockRequest request) {
        boolean success = inventoryService.decreaseStock(request);
        return Result.success("库存扣减成功", success);
    }
    
    @PostMapping("/confirm")
    public Result<Boolean> confirmStock(@Valid @RequestBody DecreaseStockRequest request) {
        boolean success = inventoryService.confirmStock(request);
        return Result.success("库存确认成功", success);
    }
    
    @PostMapping("/rollback")
    public Result<Boolean> rollbackStock(@Valid @RequestBody DecreaseStockRequest request) {
        boolean success = inventoryService.rollbackStock(request);
        return Result.success("库存回滚成功", success);
    }
    
    @PostMapping("/{productId}")
    public Result<Inventory> createInventory(@PathVariable("productId") Long productId, @RequestParam Integer stock) {
        Inventory inventory = inventoryService.createInventory(productId, stock);
        return Result.success("库存创建成功", inventory);
    }
    
    @GetMapping("/{productId}")
    public Result<Inventory> getInventory(@PathVariable("productId") Long productId) {
        Inventory inventory = inventoryService.getInventory(productId);
        return Result.success(inventory);
    }
}
