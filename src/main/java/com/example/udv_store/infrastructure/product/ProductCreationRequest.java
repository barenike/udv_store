package com.example.udv_store.infrastructure.product;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class ProductCreationRequest {

    @NotNull
    private String name;

    @NotNull
    private Integer price;

    private String description;

    @NotNull
    private Integer amount;
}
