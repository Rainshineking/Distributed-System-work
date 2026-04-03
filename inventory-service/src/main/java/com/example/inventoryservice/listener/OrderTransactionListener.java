package com.example.inventoryservice.listener;

import com.example.inventoryservice.dto.DecreaseStockRequest;
import com.example.common.dto.InventoryTransactionMessage;
import com.example.inventoryservice.service.InventoryService;
import com.example.inventoryservice.service.InventoryTransactionProducer;
import com.example.inventoryservice.util.RedisInventoryManager;
import com.example.common.dto.OrderTransactionMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * 订单事务消息监听器 - 库存服务监听订单服务的消息
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderTransactionListener {
    
    private final InventoryService inventoryService;
    private final InventoryTransactionProducer inventoryTransactionProducer;
    private final RedisInventoryManager redisInventoryManager;
    
    /**
     * 监听订单创建消息（Try 阶段）
     */
    @KafkaListener(topics = "seckill-transaction-topic", groupId = "inventory-service-group")
    @Transactional
    public void handleOrderCreatedMessage(OrderTransactionMessage message) {
        log.info("收到订单创建消息: orderId={}", message.getOrderId());
        
        // 1. 基于 Redis 实现库存预扣减
        boolean stockReserved = redisInventoryManager.tryReserveStock(
                message.getProductId(), 
                message.getQuantity()
        );
        
        if (stockReserved) {
            // 2. 发送库存预扣减成功消息
            InventoryTransactionMessage response = new InventoryTransactionMessage();
            response.setMessageId(UUID.randomUUID().toString());
            response.setMessageType(InventoryTransactionMessage.MessageType.STOCK_RESERVED);
            response.setOrderId(message.getOrderId());
            response.setProductId(message.getProductId());
            response.setUserId(message.getUserId());
            response.setQuantity(message.getQuantity());
            
            inventoryTransactionProducer.sendStockReservedMessage(response);
            log.info("库存预扣减成功，已发送确认消息: orderId={}", message.getOrderId());
        } else {
            // 3. 库存不足，发送回滚消息
            InventoryTransactionMessage response = new InventoryTransactionMessage();
            response.setMessageId(UUID.randomUUID().toString());
            response.setMessageType(InventoryTransactionMessage.MessageType.STOCK_ROLLED_BACK);
            response.setOrderId(message.getOrderId());
            response.setProductId(message.getProductId());
            response.setUserId(message.getUserId());
            response.setQuantity(message.getQuantity());
            
            inventoryTransactionProducer.sendStockRolledBackMessage(response);
            log.warn("库存预扣减失败，已发送回滚消息: orderId={}", message.getOrderId());
        }
    }
    
    /**
     * 监听订单确认消息（Confirm 阶段 - 支付成功）
     */
    @KafkaListener(topics = "seckill-transaction-topic", groupId = "inventory-service-group-confirmed")
    @Transactional
    public void handleOrderConfirmedMessage(OrderTransactionMessage message) {
        log.info("收到订单确认消息: orderId={}", message.getOrderId());
        
        // 确认库存扣减（Redis 中已经扣减，这里只是记录日志）
        redisInventoryManager.confirmStock(message.getProductId(), message.getQuantity());
        
        // 发送库存确认消息
        InventoryTransactionMessage response = new InventoryTransactionMessage();
        response.setMessageId(UUID.randomUUID().toString());
        response.setMessageType(InventoryTransactionMessage.MessageType.STOCK_CONFIRMED);
        response.setOrderId(message.getOrderId());
        response.setProductId(message.getProductId());
        response.setUserId(message.getUserId());
        response.setQuantity(message.getQuantity());
        
        inventoryTransactionProducer.sendStockConfirmedMessage(response);
        log.info("库存确认完成: orderId={}", message.getOrderId());
    }
    
    /**
     * 监听订单取消消息（Cancel 阶段）
     */
    @KafkaListener(topics = "seckill-transaction-topic", groupId = "inventory-service-group-cancelled")
    @Transactional
    public void handleOrderCancelledMessage(OrderTransactionMessage message) {
        log.info("收到订单取消消息: orderId={}", message.getOrderId());
        
        // 回滚库存
        redisInventoryManager.rollbackStock(message.getProductId(), message.getQuantity());
        
        // 发送库存回滚确认消息
        InventoryTransactionMessage response = new InventoryTransactionMessage();
        response.setMessageId(UUID.randomUUID().toString());
        response.setMessageType(InventoryTransactionMessage.MessageType.STOCK_ROLLED_BACK);
        response.setOrderId(message.getOrderId());
        response.setProductId(message.getProductId());
        response.setUserId(message.getUserId());
        response.setQuantity(message.getQuantity());
        
        inventoryTransactionProducer.sendStockRolledBackMessage(response);
        log.info("库存回滚完成: orderId={}", message.getOrderId());
    }
}