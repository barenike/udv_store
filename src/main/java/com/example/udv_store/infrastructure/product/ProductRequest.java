package com.example.udv_store.infrastructure.product;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class ProductRequest {

    @NotNull
    private String productId;
}
