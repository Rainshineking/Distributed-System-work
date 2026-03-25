package com.example.orderservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 订单消息对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderMessage implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 订单 ID
     */
    private Long orderId;
    
    /**
     * 用户 ID
     */
    private Long userId;
    
    /**
     * 商品 ID
     */
    private Long productId;
    
    /**
     * 购买数量
     */
    private Integer quantity;
    
    /**
     * 订单总价
     */
    private BigDecimal totalPrice;
    
    /**
     * 消息发送时间戳
     */
    private Long timestamp;
}
