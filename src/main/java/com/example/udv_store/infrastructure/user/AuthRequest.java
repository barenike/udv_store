package com.example.udv_store.infrastructure.user;

import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
public class AuthRequest implements Serializable {

    @NotNull
    @Max(255)
    private String email;

    @NotNull
    @Max(255)
    private String password;
}
