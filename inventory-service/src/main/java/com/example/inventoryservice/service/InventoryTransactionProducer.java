package com.example.inventoryservice.service;

import com.example.inventoryservice.config.KafkaConfig;
import com.example.common.dto.InventoryTransactionMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

/**
 * 库存事务消息生产者 - 负责发送库存事务消息
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryTransactionProducer {
    
    private final KafkaTemplate<String, InventoryTransactionMessage> kafkaTemplate;
    
    /**
     * 发送库存预扣减成功消息
     */
    public void sendStockReservedMessage(InventoryTransactionMessage message) {
        try {
            kafkaTemplate.send(KafkaConfig.INVENTORY_TOPIC, 
                    String.valueOf(message.getOrderId()), 
                    message);
            log.info("库存预扣减事务消息已发送: orderId={}", message.getOrderId());
        } catch (Exception e) {
            log.error("发送库存预扣减事务消息失败: orderId={}, error={}", 
                    message.getOrderId(), e.getMessage(), e);
            throw new RuntimeException("库存事务消息发送失败", e);
        }
    }
    
    /**
     * 发送库存确认消息
     */
    public void sendStockConfirmedMessage(InventoryTransactionMessage message) {
        try {
            kafkaTemplate.send(KafkaConfig.INVENTORY_TOPIC, 
                    String.valueOf(message.getOrderId()), 
                    message);
            log.info("库存确认事务消息已发送: orderId={}", message.getOrderId());
        } catch (Exception e) {
            log.error("发送库存确认事务消息失败: orderId={}, error={}", 
                    message.getOrderId(), e.getMessage(), e);
            throw new RuntimeException("库存事务消息发送失败", e);
        }
    }
    
    /**
     * 发送库存回滚消息
     */
    public void sendStockRolledBackMessage(InventoryTransactionMessage message) {
        try {
            kafkaTemplate.send(KafkaConfig.INVENTORY_TOPIC, 
                    String.valueOf(message.getOrderId()), 
                    message);
            log.info("库存回滚事务消息已发送: orderId={}", message.getOrderId());
        } catch (Exception e) {
            log.error("发送库存回滚事务消息失败: orderId={}, error={}", 
                    message.getOrderId(), e.getMessage(), e);
            throw new RuntimeException("库存事务消息发送失败", e);
        }
    }
}