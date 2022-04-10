package com.example.udv_store.controller;

import com.example.udv_store.model.entity.OrderRecordEntity;
import com.example.udv_store.model.service.OrderRecordService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
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
    public ResponseEntity<List<OrderRecordEntity>> manipulateOrderRecords(@RequestParam(value = "orderId", required = false) UUID orderId) {
        try {
            final List<OrderRecordEntity> orderRecords;
            if (orderId == null) {
                orderRecords = orderRecordService.findAllOrderRecords();
            } else {
                orderRecords = orderRecordService.findAllOrderRecordsByOrderId(orderId);
            }
            return getListResponseEntity(orderRecords);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    private ResponseEntity<List<OrderRecordEntity>> getListResponseEntity(List<OrderRecordEntity> orders) {
        return orders != null && !orders.isEmpty()
                ? new ResponseEntity<>(orders, HttpStatus.OK)
                : new ResponseEntity<>(HttpStatus.OK);
    }
}
