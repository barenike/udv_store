package com.example.udv_store.infrastructure.user;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class InfoResponse {
    private String uuid;
    private Integer roleId;
    private String email;
    private Integer userBalance;
}
