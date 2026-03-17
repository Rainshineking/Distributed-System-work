package com.example.orderservice.service;

import com.example.common.exception.BusinessException;
import com.example.orderservice.dto.CreateOrderRequest;
import com.example.orderservice.entity.Order;
import com.example.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {
    
    private final OrderRepository orderRepository;
    
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
    
    @Transactional(readOnly = true)
    public Order getOrderById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new BusinessException("订单不存在"));
    }
    
    @Transactional(readOnly = true)
    public List<Order> getOrdersByUserId(Long userId) {
        return orderRepository.findByUserId(userId);
    }
    
    @Transactional
    public Order updateOrderStatus(Long id, Integer status) {
        Order order = getOrderById(id);
        order.setStatus(status);
        
        orderRepository.save(order);
        log.info("订单状态更新成功，订单 ID: {}, 新状态：{}", id, status);
        
        return order;
    }
    
    @Transactional
    public void deleteOrder(Long id) {
        getOrderById(id);
        orderRepository.deleteById(id);
        log.info("订单删除成功，订单 ID: {}", id);
    }
}
