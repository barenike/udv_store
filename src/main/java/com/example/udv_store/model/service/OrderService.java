package com.example.udv_store.model.service;

import com.example.udv_store.configuration.jwt.JwtProvider;
import com.example.udv_store.exceptions.NotEnoughCoinsException;
import com.example.udv_store.exceptions.ProductIsNotFoundException;
import com.example.udv_store.infrastructure.order.OrderCreationDetails;
import com.example.udv_store.infrastructure.order.OrderCreationRequest;
import com.example.udv_store.model.entity.*;
import com.example.udv_store.model.repository.OrderRepository;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
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

    protected static Date getCurrentYekaterinburgDate() {
        DateTimeZone zoneYekaterinburg = DateTimeZone.forID("Asia/Yekaterinburg");
        DateTime now = DateTime.now(zoneYekaterinburg);
        return now.toDate();
    }

    @Transactional
    public void create(OrderCreationRequest orderCreationRequest, String token) {
        OrderEntity order = new OrderEntity();
        order.setStatus(OrderStatusEnum.CREATED.toString());
        String userId = jwtProvider.getUserIdFromToken(token.substring(7));
        UserEntity user = userService.findByUserId(userId);
        order.setUserId(UUID.fromString(userId));
        order.setCreationDate(getCurrentYekaterinburgDate());
        int total = 0;
        for (OrderCreationDetails orderCreationDetails : orderCreationRequest.getOrderCreationDetails()) {
            ProductEntity product = productService.getProduct(UUID.fromString(orderCreationDetails.getProductId()));
            if (product == null) {
                throw new ProductIsNotFoundException("Product with this UUID does not exist.");
            }
            total += product.getPrice() * orderCreationDetails.getQuantity();
        }
        Integer userBalance = user.getUserBalance();
        if (total > userBalance) {
            throw new NotEnoughCoinsException("The user does not have enough coins to pay for this order.");
        }
        order.setTotal(total);
        orderRepository.save(order);
        orderRecordService.create(orderCreationRequest.getOrderCreationDetails(), order.getId());
        productService.changeProductAmount(orderCreationRequest.getOrderCreationDetails());
        userService.changeUserBalance(user, userBalance - total);
    }

    public boolean changeStatus(UUID id, String status) {
        if (orderRepository.existsById(id)) {
            OrderEntity order = orderRepository.getById(id);
            order.setStatus(OrderStatusEnum.valueOf(status).toString());
            if (OrderStatusEnum.SHIPPED.toString().equals(status)) {
                order.setShippingDate(getCurrentYekaterinburgDate());
            } else if (OrderStatusEnum.COMPLETED.toString().equals(status)) {
                order.setCompletionDate(getCurrentYekaterinburgDate());
            }
            orderRepository.save(order);
            return true;
        } else {
            return false;
        }
    }

    public List<OrderEntity> findAllOrders() {
        return orderRepository.findAll();
    }

    public List<OrderEntity> findOrdersByUserId(UUID userId) {
        return orderRepository.findByUserId(userId);
    }

    @Transactional
    public boolean delete(UUID id) throws Exception {
        if (orderRepository.existsById(id)) {
            OrderEntity order = orderRepository.getById(id);
            if (!order.getStatus().equals(OrderStatusEnum.CREATED.toString())) {
                return false;
            }
            String userId = String.valueOf(order.getUserId());
            UserEntity user = userService.findByUserId(userId);
            Integer userBalance = user.getUserBalance();
            Integer total = order.getTotal();
            List<OrderRecordEntity> orderRecords = orderRecordService.findOrderRecordsByOrderId(id);
            for (OrderRecordEntity orderRecord : orderRecords) {
                ProductEntity product = productService.getProduct(orderRecord.getProductId());
                if (product == null) {
                    throw new ProductIsNotFoundException("Product with this UUID does not exist.");
                }
                Integer quantity = orderRecord.getQuantity();
                boolean isDeleted = orderRecordService.delete(orderRecord.getId());
                if (!isDeleted) {
                    throw new Exception();
                }
                productService.changeProductAmount(product, product.getAmount() + quantity);
            }
            orderRepository.deleteById(id);
            userService.changeUserBalance(user, userBalance + total);
            return true;
        } else {
            return false;
        }
    }
}
