package com.example.udv_store.infrastructure.order_record;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class OrderRecordCreationRequest {
    @NotNull
    private String orderId;

    @NotNull
    private String productId;

    @NotNull
    private Integer quantity;
}
