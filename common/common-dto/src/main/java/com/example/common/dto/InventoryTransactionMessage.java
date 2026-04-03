package com.example.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 库存事务消息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryTransactionMessage implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 消息类型
     */
    public enum MessageType {
        STOCK_RESERVED,     // 库存预扣减成功
        STOCK_CONFIRMED,    // 库存确认扣减
        STOCK_ROLLED_BACK   // 库存回滚
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
     * 商品ID
     */
    private Long productId;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 数量
     */
    private Integer quantity;
    
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