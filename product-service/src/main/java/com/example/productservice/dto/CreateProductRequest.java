package com.example.productservice.dto;

import lombok.Data;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
public class CreateProductRequest {
    
    @NotBlank(message = "商品名称不能为空")
    private String name;
    
    @NotNull(message = "商品价格不能为空")
    @DecimalMin(value = "0.01", message = "价格必须大于 0")
    private BigDecimal price;
    
    private String description;
    
    private String imageUrl;
}
