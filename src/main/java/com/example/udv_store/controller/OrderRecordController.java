package com.example.udv_store.controller;

import com.example.udv_store.exceptions.OrderRecordIsNotFoundException;
import com.example.udv_store.model.entity.OrderRecordEntity;
import com.example.udv_store.model.service.OrderRecordService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
public class OrderRecordController {
    private final OrderRecordService orderRecordService;

    public OrderRecordController(OrderRecordService orderRecordService) {
        this.orderRecordService = orderRecordService;
    }

    @GetMapping("/user/order_records/{orderId}")
    public ResponseEntity<List<OrderRecordEntity>> getMyOrderRecordsByOrderId(@PathVariable(name = "orderId") UUID orderId) {
        try {
            final List<OrderRecordEntity> orders = orderRecordService.findOrderRecordsByOrderId(orderId);
            return orders != null && !orders.isEmpty()
                    ? new ResponseEntity<>(orders, HttpStatus.OK)
                    : new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/admin/order_records")
    public ResponseEntity<List<OrderRecordEntity>> getOrderRecords() {
        try {
            List<OrderRecordEntity> orderRecords = orderRecordService.findAllOrderRecords();
            return orderRecords != null && !orderRecords.isEmpty()
                    ? new ResponseEntity<>(orderRecords, HttpStatus.OK)
                    : new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/admin/order_records/{orderRecordId}")
    public ResponseEntity<?> getOrderRecordById(@PathVariable(name = "orderRecordId") UUID orderRecordId) {
        try {
            List<OrderRecordEntity> orderRecords = orderRecordService.findAllOrderRecordsById(orderRecordId);
            if (orderRecords.isEmpty()) {
                throw new OrderRecordIsNotFoundException("Order record with this UUID does not exist.");
            }
            return new ResponseEntity<>(orderRecords, HttpStatus.OK);
        } catch (OrderRecordIsNotFoundException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }
}
