package com.example.udv_store.model.service;

import com.example.udv_store.model.entity.OrderEntity;
import com.example.udv_store.model.repository.OrderRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class OrderService {
    private final OrderRepository orderRepository;

    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public void create(OrderEntity order) {
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
