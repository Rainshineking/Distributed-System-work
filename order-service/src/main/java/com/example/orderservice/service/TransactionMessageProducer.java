package com.example.orderservice.service;

import com.example.orderservice.config.KafkaConfig;
import com.example.common.dto.OrderTransactionMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

/**
 * 事务消息生产者 - 负责发送订单事务消息
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionMessageProducer {
    
    private final KafkaTemplate<String, OrderTransactionMessage> kafkaTemplate;
    
    /**
     * 发送订单创建消息
     */
    public void sendOrderCreatedMessage(OrderTransactionMessage message) {
        try {
            kafkaTemplate.send(KafkaConfig.TRANSACTION_TOPIC, 
                    String.valueOf(message.getOrderId()), 
                    message);
            log.info("订单创建事务消息已发送: orderId={}", message.getOrderId());
        } catch (Exception e) {
            log.error("发送订单创建事务消息失败: orderId={}, error={}", 
                    message.getOrderId(), e.getMessage(), e);
            throw new RuntimeException("事务消息发送失败", e);
        }
    }
    
    /**
     * 发送订单确认消息（支付成功）
     */
    public void sendOrderConfirmedMessage(OrderTransactionMessage message) {
        try {
            kafkaTemplate.send(KafkaConfig.TRANSACTION_TOPIC, 
                    String.valueOf(message.getOrderId()), 
                    message);
            log.info("订单确认事务消息已发送: orderId={}", message.getOrderId());
        } catch (Exception e) {
            log.error("发送订单确认事务消息失败: orderId={}, error={}", 
                    message.getOrderId(), e.getMessage(), e);
            throw new RuntimeException("事务消息发送失败", e);
        }
    }
    
    /**
     * 发送订单取消消息
     */
    public void sendOrderCancelledMessage(OrderTransactionMessage message) {
        try {
            kafkaTemplate.send(KafkaConfig.TRANSACTION_TOPIC, 
                    String.valueOf(message.getOrderId()), 
                    message);
            log.info("订单取消事务消息已发送: orderId={}", message.getOrderId());
        } catch (Exception e) {
            log.error("发送订单取消事务消息失败: orderId={}, error={}", 
                    message.getOrderId(), e.getMessage(), e);
            throw new RuntimeException("事务消息发送失败", e);
        }
    }
}