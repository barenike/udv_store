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
    public ResponseEntity<List<OrderRecordEntity>> getMyOrderRecords(@PathVariable(name = "orderId") UUID orderId) {
        try {
            final List<OrderRecordEntity> orders = orderRecordService.findOrderRecordsByOrderId(orderId);
            return orders != null && !orders.isEmpty()
                    ? new ResponseEntity<>(orders, HttpStatus.OK)
                    : new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    // Yet to be tested
    @GetMapping("/admin/order_records")
    public ResponseEntity<List<OrderRecordEntity>> manipulateOrderRecords(@RequestParam(value = "userId", required = false) UUID userId,
                                                                          @RequestParam(value = "orderRecordId", required = false) UUID orderRecordId) {
        try {
            if (userId == null && orderRecordId == null) {
                final List<OrderRecordEntity> orderRecords = orderRecordService.findAllOrderRecords();
                return getListResponseEntity(orderRecords);
            } else if (userId != null) {
                final List<OrderRecordEntity> orderRecords = orderRecordService.findAllOrderRecordsByOrderId(userId);
                return getListResponseEntity(orderRecords);
            } else {
                final boolean isDeleted = orderRecordService.delete(orderRecordId);
                return isDeleted
                        ? new ResponseEntity<>(HttpStatus.OK)
                        : new ResponseEntity<>(HttpStatus.NOT_MODIFIED);
            }
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
