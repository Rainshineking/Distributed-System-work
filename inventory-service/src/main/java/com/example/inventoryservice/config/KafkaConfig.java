package com.example.inventoryservice.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

/**
 * Kafka 配置 - 库存服务
 */
@Configuration
public class KafkaConfig {
    
    public static final String ORDER_TOPIC = "seckill-order-topic";
    public static final String TRANSACTION_TOPIC = "seckill-transaction-topic";
    public static final String INVENTORY_TOPIC = "seckill-inventory-topic";
    
    /**
     * 创建库存主题（库存服务发送消息给订单服务）
     */
    @Bean
    public NewTopic inventoryTopic() {
        return TopicBuilder.name(INVENTORY_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }
}