package com.example.udv_store.infrastructure.user;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class UserBalanceRequest {

    @NotNull
    private Integer tokenBalance;

    @NotNull
    private String id;
}
