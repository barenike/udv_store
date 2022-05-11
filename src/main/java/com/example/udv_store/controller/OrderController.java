package com.example.udv_store.controller;

import com.example.udv_store.configuration.jwt.JwtProvider;
import com.example.udv_store.exceptions.NotEnoughCoinsException;
import com.example.udv_store.exceptions.ProductIsNotFoundException;
import com.example.udv_store.infrastructure.order.OrderCreationRequest;
import com.example.udv_store.model.entity.OrderEntity;
import com.example.udv_store.model.entity.UserEntity;
import com.example.udv_store.model.service.OrderService;
import com.example.udv_store.model.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.UUID;

@RestController
public class OrderController {
    private final OrderService orderService;
    private final UserService userService;
    private final JwtProvider jwtProvider;

    public OrderController(OrderService orderService, UserService userService, JwtProvider jwtProvider) {
        this.orderService = orderService;
        this.userService = userService;
        this.jwtProvider = jwtProvider;
    }

    @PostMapping("/user/orders")
    public ResponseEntity<?> createOrder(@RequestBody @Valid OrderCreationRequest orderCreationRequest,
                                         @RequestHeader(name = "Authorization") String token) {
        try {
            orderService.create(orderCreationRequest, token);
            return new ResponseEntity<>(HttpStatus.CREATED);
        } catch (NotEnoughCoinsException | ProductIsNotFoundException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/user/orders")
    public ResponseEntity<?> getMyOrders(@RequestHeader(name = "Authorization") String token) {
        try {
            String userId = jwtProvider.getUserIdFromToken(token.substring(7));
            UserEntity user = userService.findByUserId(userId);
            final List<OrderEntity> orders = orderService.findOrdersByUserId(user.getId());
            return orders != null && !orders.isEmpty()
                    ? new ResponseEntity<>(orders, HttpStatus.OK)
                    : new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/user/orders/{orderId}")
    public ResponseEntity<?> deleteMyOrder(@PathVariable(name = "orderId") UUID orderID) {
        try {
            boolean isDeleted = orderService.delete(orderID);
            return isDeleted
                    ? new ResponseEntity<>(HttpStatus.OK)
                    : new ResponseEntity<>(HttpStatus.NOT_MODIFIED);
        } catch (ProductIsNotFoundException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/admin/orders")
    public ResponseEntity<?> manipulateOrders(@RequestParam(value = "userId", required = false) UUID userId,
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
        } catch (ProductIsNotFoundException e) {
            /*
             * Is it possible that we have order with a product that doesn't exist!?
             * Database won't allow deleting of a product that is employed in at least one order?
             * Or will allow? Find out the answer.
             */
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
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
