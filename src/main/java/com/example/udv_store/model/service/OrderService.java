package com.example.udv_store.model.service;

import com.example.udv_store.configuration.jwt.JwtProvider;
import com.example.udv_store.infrastructure.order.OrderCreationDetails;
import com.example.udv_store.infrastructure.order.OrderCreationRequest;
import com.example.udv_store.model.entity.OrderEntity;
import com.example.udv_store.model.entity.OrderRecordEntity;
import com.example.udv_store.model.entity.UserEntity;
import com.example.udv_store.model.repository.OrderRepository;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class OrderService {
    private final UserService userService;
    private final ProductService productService;
    private final OrderRecordService orderRecordService;
    private final OrderRepository orderRepository;
    private final JwtProvider jwtProvider;

    public OrderService(UserService userService, ProductService productService, OrderRecordService orderRecordService, OrderRepository orderRepository, JwtProvider jwtProvider) {
        this.userService = userService;
        this.productService = productService;
        this.orderRecordService = orderRecordService;
        this.orderRepository = orderRepository;
        this.jwtProvider = jwtProvider;
    }

    public void create(OrderCreationRequest orderCreationRequest, String token) {
        OrderEntity order = new OrderEntity();
        String userId = jwtProvider.getUserIdFromToken(token.substring(7));
        order.setUserId(UUID.fromString(userId));
        DateTimeZone zoneYekaterinburg = DateTimeZone.forID("Asia/Yekaterinburg");
        DateTime now = DateTime.now(zoneYekaterinburg);
        order.setOrderDate(now.toDate());
        int total = 0;
        for (OrderCreationDetails orderCreationDetails : orderCreationRequest.getOrderCreationDetails()) {
            total += productService.getProduct(UUID.fromString(orderCreationDetails.getProductId())).getPrice() * orderCreationDetails.getQuantity();
        }
        UserEntity user = userService.findByUserId(userId);
        Integer userBalance = user.getUserBalance();
        if (total > userBalance) {
            throw new IllegalArgumentException();
        }
        order.setTotal(total);
        orderRepository.save(order);
        orderRecordService.create(orderCreationRequest.getOrderCreationDetails(), order.getId());
        userService.changeUserBalance(userId, userBalance - total);
    }

    public List<OrderEntity> findAllOrders() {
        return orderRepository.findAll();
    }

    public List<OrderEntity> findOrdersByUserId(UUID userId) {
        return orderRepository.findByUserId(userId);
    }

    public boolean delete(UUID id) throws Exception {
        if (orderRepository.existsById(id)) {
            OrderEntity order = orderRepository.getById(id);
            String userId = String.valueOf(order.getUserId());
            UserEntity user = userService.findByUserId(userId);
            Integer userBalance = user.getUserBalance();
            Integer total = order.getTotal();
            List<OrderRecordEntity> orderRecords = orderRecordService.findAllOrderRecordsByOrderId(id);
            for (OrderRecordEntity orderRecord : orderRecords) {
                boolean isDeleted = orderRecordService.delete(orderRecord.getId());
                if (!isDeleted) {
                    throw new Exception();
                }
            }
            orderRepository.deleteById(id);
            userService.changeUserBalance(userId, userBalance + total);
            return true;
        } else {
            return false;
        }
    }
}
