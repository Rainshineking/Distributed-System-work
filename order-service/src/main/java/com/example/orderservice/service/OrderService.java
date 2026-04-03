package com.example.orderservice.service;

import com.example.common.exception.BusinessException;
import com.example.orderservice.dto.CreateOrderRequest;
import com.example.orderservice.dto.OrderMessage;
import com.example.orderservice.entity.Order;
import com.example.orderservice.repository.OrderRepository;
import com.example.orderservice.util.IdempotencyUtil;
import com.example.orderservice.util.SnowflakeIdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {
    
    private final OrderRepository orderRepository;
    private final SnowflakeIdGenerator snowflakeIdGenerator;
    private final IdempotencyUtil idempotencyUtil;
    private final StringRedisTemplate redisTemplate;
    private final OrderKafkaProducer orderKafkaProducer;
    
    /**
     * 异步创建订单（秒杀下单）
     * 1. 检查幂等性 - 防止重复下单
     * 2. 生成订单 ID（雪花算法）
     * 3. 发送消息到 MQ，异步处理
     * 4. 返回订单 ID
     */
    @Transactional
    public Long createSeckillOrder(CreateOrderRequest request) {
        Long userId = request.getUserId();
        Long productId = request.getProductId();
        
        // 1. 幂等性检查：同一用户同一商品只能下单一次
        String idempotencyKey = "seckill:order:userId:" + userId + ":productId:" + productId;
        
        if (idempotencyUtil.exists(idempotencyKey)) {
            log.warn("重复下单请求：userId={}, productId={}", userId, productId);
            throw new BusinessException("您已经下单成功，请勿重复下单");
        }
        
        // 2. 使用雪花算法生成订单 ID
        Long orderId = snowflakeIdGenerator.nextId();
        
        // 3. 设置幂等性锁（24 小时过期）
        boolean locked = idempotencyUtil.trySetIfAbsent(
                idempotencyKey, 
                orderId.toString(), 
                24, 
                TimeUnit.HOURS
        );
        
        if (!locked) {
            throw new BusinessException("下单失败，请稍后重试");
        }
        
        // 4. 创建订单消息
        OrderMessage message = new OrderMessage();
        message.setOrderId(orderId);
        message.setUserId(userId);
        message.setProductId(productId);
        message.setQuantity(request.getQuantity());
        message.setTotalPrice(request.getTotalPrice());
        message.setTimestamp(System.currentTimeMillis());
        
        // 5. 发送消息到 Kafka（异步处理）
        orderKafkaProducer.sendOrderMessage(message);
        
        log.info("秒杀订单创建成功，订单 ID: {}, 用户 ID: {}, 商品 ID: {}, 已发送 Kafka 异步处理", 
                orderId, userId, productId);
        
        return orderId;
    }
    
    /**
     * 同步创建订单（普通下单）
     */
    @Transactional
    public Order createOrder(CreateOrderRequest request) {
        Order order = new Order();
        order.setUserId(request.getUserId());
        order.setProductId(request.getProductId());
        order.setQuantity(request.getQuantity());
        order.setTotalPrice(request.getTotalPrice());
        
        orderRepository.save(order);
        log.info("订单创建成功，订单 ID: {}, 用户 ID: {}, 商品 ID: {}", 
                order.getId(), order.getUserId(), order.getProductId());
        
        return order;
    }
    
    /**
     * 根据订单 ID 查询订单
     */
    @Transactional(readOnly = true)
    public Order getOrderById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new BusinessException("订单不存在"));
    }
    
    /**
     * 根据用户 ID 查询订单列表
     */
    @Transactional(readOnly = true)
    public List<Order> getOrdersByUserId(Long userId) {
        return orderRepository.findByUserId(userId);
    }
    
    /**
     * 更新订单状态
     */
    @Transactional
    public Order updateOrderStatus(Long id, Integer status) {
        Order order = getOrderById(id);
        order.setStatus(status);
        
        orderRepository.save(order);
        log.info("订单状态更新成功，订单 ID: {}, 新状态：{}", id, status);
        
        return order;
    }
    
    /**
     * 删除订单
     */
    @Transactional
    public void deleteOrder(Long id) {
        getOrderById(id);
        orderRepository.deleteById(id);
        log.info("订单删除成功，订单 ID: {}", id);
    }
}
