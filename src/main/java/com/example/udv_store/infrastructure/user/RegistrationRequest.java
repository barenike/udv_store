package com.example.udv_store.infrastructure.user;

import lombok.Data;

import javax.validation.constraints.Pattern;
import java.io.Serializable;

@Data
public class RegistrationRequest implements Serializable {
    // For testing, return to @ussc\\.ru in production
    @Pattern(regexp = "[\\w!#$%&'.*+/=?^`{|}~-]*@mail\\.ru")
    private String email;

    @Pattern(regexp = "(?=.*?[a-z])(?=.*?[A-Z])(?=.*?\\d)\\S{8,255}")
    private String password;
}
