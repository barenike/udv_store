package com.example.udv_store.model.service;

import com.example.udv_store.model.repository.OrderRepository;
import org.springframework.stereotype.Service;

@Service
public class OrderRecordService {
    private final OrderRepository orderRepository;

    public OrderRecordService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }
}
