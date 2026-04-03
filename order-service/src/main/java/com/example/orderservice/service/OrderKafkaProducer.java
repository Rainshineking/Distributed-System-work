package com.example.orderservice.service;

import com.example.orderservice.config.KafkaConfig;
import com.example.orderservice.dto.OrderMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

/**
 * Kafka 订单消息生产者
 */
@Slf4j
@Service
public class OrderKafkaProducer {
    
    private final KafkaTemplate<String, OrderMessage> kafkaTemplate;
    
    public OrderKafkaProducer(KafkaTemplate<String, OrderMessage> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }
    
    /**
     * 发送订单消息到 Kafka
     */
    public void sendOrderMessage(OrderMessage message) {
        try {
            kafkaTemplate.send(KafkaConfig.ORDER_TOPIC, 
                    String.valueOf(message.getOrderId()), 
                    message);
            log.info("订单消息已发送到 Kafka: orderId={}", message.getOrderId());
        } catch (Exception e) {
            log.error("发送订单消息到 Kafka 失败: orderId={}, error={}", 
                    message.getOrderId(), e.getMessage(), e);
            throw new RuntimeException("消息发送失败", e);
        }
    }
}