package com.example.orderservice.dto;

import lombok.Data;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
public class CreateOrderRequest {
    
    @NotNull(message = "用户 ID 不能为空")
    private Long userId;
    
    @NotNull(message = "商品 ID 不能为空")
    private Long productId;
    
    @NotNull(message = "数量不能为空")
    @Min(value = 1, message = "数量必须大于 0")
    private Integer quantity;
    
    @NotNull(message = "总价不能为空")
    private BigDecimal totalPrice;
}
