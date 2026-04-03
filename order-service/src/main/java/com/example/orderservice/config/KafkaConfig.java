package com.example.orderservice.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

/**
 * Kafka 配置
 */
@Configuration
public class KafkaConfig {
    
    public static final String ORDER_TOPIC = "seckill-order-topic";
    
    /**
     * 创建订单主题
     */
    @Bean
    public NewTopic orderTopic() {
        return TopicBuilder.name(ORDER_TOPIC)
                .partitions(3)  // 3个分区，提高并发处理能力
                .replicas(1)    // 1个副本（开发环境）
                .build();
    }
}