package com.example.orderservice.listener;

import com.example.orderservice.config.KafkaConfig;
import com.example.orderservice.dto.OrderMessage;
import com.example.orderservice.entity.Order;
import com.example.orderservice.repository.OrderRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * Kafka 订单消息监听器 - 异步处理订单创建
 */
@Slf4j
@Component
public class OrderKafkaListener {
    
    private final OrderRepository orderRepository;
    
    public OrderKafkaListener(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }
    
    /**
     * 监听订单创建消息
     */
    @KafkaListener(topics = KafkaConfig.ORDER_TOPIC, groupId = "order-group")
    public void handleOrderMessage(OrderMessage message) {
        try {
            log.info("收到订单创建消息：orderId={}, userId={}, productId={}", 
                    message.getOrderId(), message.getUserId(), message.getProductId());
            
            // 创建订单
            Order order = new Order();
            order.setId(message.getOrderId());
            order.setUserId(message.getUserId());
            order.setProductId(message.getProductId());
            order.setQuantity(message.getQuantity());
            order.setTotalPrice(message.getTotalPrice());
            order.setStatus(0); // 待支付状态
            order.setCreateTime(new Date());
            order.setUpdateTime(new Date());
            
            // 保存订单到数据库
            orderRepository.save(order);
            
            log.info("订单创建成功：orderId={}, userId={}, productId={}", 
                    order.getId(), order.getUserId(), order.getProductId());
            
        } catch (Exception e) {
            log.error("处理订单消息失败：orderId={}, error={}", 
                    message.getOrderId(), e.getMessage(), e);
            // 这里可以添加重试逻辑或死信队列处理
            throw e; // 抛出异常让消息重回队列
        }
    }
}