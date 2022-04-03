package com.example.udv_store.infrastructure.user;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class TokenBalanceRequest {

    @NotNull
    private Integer tokenBalance;

    @NotNull
    private String id;
}
