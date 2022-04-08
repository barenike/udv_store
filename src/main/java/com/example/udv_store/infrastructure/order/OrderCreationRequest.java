package com.example.udv_store.infrastructure.order;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class OrderCreationRequest {

    @NotNull
    private Integer total;
}
