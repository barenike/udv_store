package com.example.udv_store.infrastructure.user;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class AdminInfoResponse {
    private List<InfoResponse> infoList;
}
