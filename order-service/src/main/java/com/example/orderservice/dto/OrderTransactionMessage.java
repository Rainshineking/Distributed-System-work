package com.example.orderservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 订单创建事务消息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderTransactionMessage implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 消息类型
     */
    public enum MessageType {
        ORDER_CREATED,      // 订单创建
        ORDER_CONFIRMED,    // 订单确认（支付成功）
        ORDER_CANCELLED     // 订单取消
    }
    
    /**
     * 消息ID（用于幂等性）
     */
    private String messageId;
    
    /**
     * 消息类型
     */
    private MessageType messageType;
    
    /**
     * 订单ID
     */
    private Long orderId;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 商品ID
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
     * 重试次数
     */
    private int retryCount = 0;
    
    /**
     * 最大重试次数
     */
    private static final int MAX_RETRY_COUNT = 3;
    
    /**
     * 是否需要重试
     */
    public boolean shouldRetry() {
        return retryCount < MAX_RETRY_COUNT;
    }
    
    /**
     * 增加重试次数
     */
    public void incrementRetryCount() {
        this.retryCount++;
    }
}