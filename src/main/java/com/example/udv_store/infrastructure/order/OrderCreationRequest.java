package com.example.udv_store.infrastructure.order;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.Date;

@Data
public class OrderCreationRequest {

    @NotNull
    private Date orderDate;

    @NotNull
    private Date deliveryDate;

    @NotNull
    private Integer total;
}
