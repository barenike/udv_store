package com.example.udv_store.infrastructure.user;

import lombok.Data;

import javax.validation.constraints.Pattern;
import java.io.Serializable;

@Data
public class RegistrationRequest implements Serializable {
    @Pattern(regexp = "[\\w!#$%&'.*+/=?^`{|}~-]*@ussc\\.ru")
    private String email;

    @Pattern(regexp = "(?=.*?[a-z])(?=.*?[A-Z])(?=.*?\\d)\\S{8,255}")
    private String password;
}
