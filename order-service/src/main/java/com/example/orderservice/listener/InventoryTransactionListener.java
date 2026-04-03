package com.example.orderservice.listener;

import com.example.common.dto.InventoryTransactionMessage;
import com.example.orderservice.entity.Order;
import com.example.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 库存事务消息监听器 - 订单服务监听库存服务的消息
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryTransactionListener {
    
    private final OrderRepository orderRepository;
    
    /**
     * 监听库存预扣减成功消息
     */
    @KafkaListener(topics = "seckill-inventory-topic", groupId = "order-service-group")
    @Transactional
    public void handleStockReservedMessage(InventoryTransactionMessage message) {
        log.info("收到库存预扣减成功消息: orderId={}", message.getOrderId());
        
        // 更新订单状态为待支付
        Order order = orderRepository.findById(message.getOrderId())
                .orElseThrow(() -> new RuntimeException("订单不存在: " + message.getOrderId()));
        
        if (order.getStatus() == 0) { // 待创建状态
            order.setStatus(1); // 待支付状态
            orderRepository.save(order);
            log.info("订单状态更新为待支付: orderId={}", message.getOrderId());
        }
    }
    
    /**
     * 监听库存确认消息（订单支付成功）
     */
    @KafkaListener(topics = "seckill-inventory-topic", groupId = "order-service-group-confirmed")
    @Transactional
    public void handleStockConfirmedMessage(InventoryTransactionMessage message) {
        log.info("收到库存确认消息: orderId={}", message.getOrderId());
        
        // 更新订单状态为已完成
        Order order = orderRepository.findById(message.getOrderId())
                .orElseThrow(() -> new RuntimeException("订单不存在: " + message.getOrderId()));
        
        if (order.getStatus() == 1) { // 待支付状态
            order.setStatus(2); // 已完成状态
            orderRepository.save(order);
            log.info("订单状态更新为已完成: orderId={}", message.getOrderId());
        }
    }
    
    /**
     * 监听库存回滚消息（订单取消或超时）
     */
    @KafkaListener(topics = "seckill-inventory-topic", groupId = "order-service-group-cancelled")
    @Transactional
    public void handleStockRolledBackMessage(InventoryTransactionMessage message) {
        log.info("收到库存回滚消息: orderId={}", message.getOrderId());
        
        // 更新订单状态为已取消
        Order order = orderRepository.findById(message.getOrderId())
                .orElseThrow(() -> new RuntimeException("订单不存在: " + message.getOrderId()));
        
        if (order.getStatus() <= 1) { // 待创建或待支付状态
            order.setStatus(3); // 已取消状态
            orderRepository.save(order);
            log.info("订单状态更新为已取消: orderId={}", message.getOrderId());
        }
    }
}