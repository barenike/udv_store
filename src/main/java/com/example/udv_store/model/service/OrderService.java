package com.example.udv_store.model.service;

import com.example.udv_store.configuration.jwt.JwtProvider;
import com.example.udv_store.infrastructure.order.OrderCreationRequest;
import com.example.udv_store.model.entity.OrderEntity;
import com.example.udv_store.model.repository.OrderRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class OrderService {
    private final OrderRepository orderRepository;
    private final JwtProvider jwtProvider;

    public OrderService(OrderRepository orderRepository, JwtProvider jwtProvider) {
        this.orderRepository = orderRepository;
        this.jwtProvider = jwtProvider;
    }

    public void create(OrderCreationRequest orderCreationRequest, String token) {
        OrderEntity order = new OrderEntity();
        String userId = jwtProvider.getUserIdFromToken(token.substring(7));
        order.setUserId(UUID.fromString(userId));
        order.setOrderDate(orderCreationRequest.getOrderDate());
        order.setDeliveryDate(orderCreationRequest.getDeliveryDate());
        order.setTotal(orderCreationRequest.getTotal());
        orderRepository.save(order);
    }

    public List<OrderEntity> findAllOrders() {
        return orderRepository.findAll();
    }

    public List<OrderEntity> findAllUserOrdersById(UUID userId) {
        List<OrderEntity> allOrders = findAllOrders();
        return allOrders.stream().filter(order -> order.getUserId().equals(userId)).collect(Collectors.toList());
    }

    public boolean delete(UUID id) {
        if (orderRepository.existsById(id)) {
            orderRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
