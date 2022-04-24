package com.example.udv_store.infrastructure.order;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class OrderCreationRequest {
    @NotNull
    @NotEmpty
    List<OrderCreationDetails> orderCreationDetails;
}
