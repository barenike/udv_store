package com.example.udv_store;

import com.example.udv_store.configuration.jwt.JwtProvider;
import com.example.udv_store.controller.UserController;
import com.example.udv_store.infrastructure.user.AuthRequest;
import com.example.udv_store.infrastructure.user.AuthResponse;
import com.example.udv_store.infrastructure.user.RegistrationRequest;
import com.example.udv_store.model.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class UserControllerTest {
    @InjectMocks
    UserController userController;

    @Mock
    UserService userService;

    @Mock
    JwtProvider jwtProvider;

    @Test
    public void registerUserTest1() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        RegistrationRequest registrationRequest = new RegistrationRequest();
        registrationRequest.setEmail("haan@udv.ru");
        registrationRequest.setPassword("123456Aa");

        ResponseEntity<?> responseEntity = userController.registerUser(registrationRequest);

        assertThat(responseEntity.getStatusCodeValue()).isEqualTo(201);
    }

    @Test
    public void registerUserTest2() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        RegistrationRequest registrationRequest = new RegistrationRequest();
        registrationRequest.setEmail("haan@udv.ru");
        registrationRequest.setPassword("123456Aa");

        userController.registerUser(registrationRequest);
        ResponseEntity<?> responseEntity = userController.registerUser(registrationRequest);

        assertThat(responseEntity.getStatusCodeValue()).isEqualTo(403);
    }

    @Test
    public void authUserTest() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        RegistrationRequest registrationRequest = new RegistrationRequest();
        registrationRequest.setEmail("haan@udv.ru");
        registrationRequest.setPassword("123456Aa");

        userController.registerUser(registrationRequest);

        AuthRequest authRequest = new AuthRequest();
        authRequest.setEmail("haan@udv.ru");
        authRequest.setPassword("123456Aa");

        ResponseEntity<AuthResponse> responseEntity = userController.auth(authRequest);

        assertThat(responseEntity.getStatusCodeValue()).isEqualTo(200);
    }
}
