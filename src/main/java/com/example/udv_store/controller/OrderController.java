package com.example.udv_store.controller;

import com.example.udv_store.configuration.jwt.JwtProvider;
import com.example.udv_store.infrastructure.order.OrderCreationRequest;
import com.example.udv_store.model.entity.OrderEntity;
import com.example.udv_store.model.service.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
public class OrderController {
    private final OrderService orderService;
    private final JwtProvider jwtProvider;

    public OrderController(OrderService orderService, JwtProvider jwtProvider) {
        this.orderService = orderService;
        this.jwtProvider = jwtProvider;
    }

    @PostMapping("/user/orders")
    public ResponseEntity<?> createOrder(@RequestBody OrderCreationRequest orderCreationRequest,
                                         @RequestHeader(name = "Authorization") String token) {
        try {
            orderService.create(orderCreationRequest, token);
            return new ResponseEntity<>(HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/user/orders")
    public ResponseEntity<List<OrderEntity>> getMyOrders(@RequestHeader(name = "Authorization") String token) {
        try {
            String userId = jwtProvider.getUserIdFromToken(token.substring(7));
            final List<OrderEntity> orders = orderService.findOrdersByUserId(UUID.fromString(userId));
            return orders != null && !orders.isEmpty()
                    ? new ResponseEntity<>(orders, HttpStatus.OK)
                    : new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/user/orders/{orderId}")
    public ResponseEntity<List<OrderEntity>> deleteMyOrder(@PathVariable(name = "orderId") UUID orderID) {
        try {
            boolean isDeleted = orderService.delete(orderID);
            return isDeleted
                    ? new ResponseEntity<>(HttpStatus.OK)
                    : new ResponseEntity<>(HttpStatus.NOT_MODIFIED);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/admin/orders")
    public ResponseEntity<List<OrderEntity>> manipulateOrders(@RequestParam(value = "userId", required = false) UUID userId,
                                                              @RequestParam(value = "orderId", required = false) UUID orderId,
                                                              @RequestParam(value = "status", required = false) String status) {
        try {
            if (userId == null && orderId == null) {
                final List<OrderEntity> orders = orderService.findAllOrders();
                return getListResponseEntity(orders);
            } else if (userId != null) {
                final List<OrderEntity> orders = orderService.findOrdersByUserId(userId);
                return getListResponseEntity(orders);
            } else if (status != null) {
                final boolean isChanged = orderService.changeStatus(orderId, status);
                return isChanged
                        ? new ResponseEntity<>(HttpStatus.OK)
                        : new ResponseEntity<>(HttpStatus.NOT_MODIFIED);
            } else {
                final boolean isDeleted = orderService.delete(orderId);
                return isDeleted
                        ? new ResponseEntity<>(HttpStatus.OK)
                        : new ResponseEntity<>(HttpStatus.NOT_MODIFIED);
            }
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    private ResponseEntity<List<OrderEntity>> getListResponseEntity(List<OrderEntity> orders) {
        return orders != null && !orders.isEmpty()
                ? new ResponseEntity<>(orders, HttpStatus.OK)
                : new ResponseEntity<>(HttpStatus.OK);
    }
}
