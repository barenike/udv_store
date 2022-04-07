package com.example.udv_store.controller;

import com.example.udv_store.configuration.jwt.JwtProvider;
import com.example.udv_store.infrastructure.user.AuthRequest;
import com.example.udv_store.infrastructure.user.AuthResponse;
import com.example.udv_store.infrastructure.user.RegistrationRequest;
import com.example.udv_store.infrastructure.user.UserBalanceRequest;
import com.example.udv_store.model.entity.UserEntity;
import com.example.udv_store.model.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {
    private final UserService userService;
    private final JwtProvider jwtProvider;

    public UserController(UserService userService, JwtProvider jwtProvider) {
        this.userService = userService;
        this.jwtProvider = jwtProvider;
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody RegistrationRequest registrationRequest) {
        try {
            userService.create(registrationRequest);
            return new ResponseEntity<>(HttpStatus.CREATED);
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/auth")
    public ResponseEntity<AuthResponse> auth(@RequestBody AuthRequest authRequest) {
        try {
            UserEntity userEntity = userService.findByEmailAndPassword(authRequest.getEmail(), authRequest.getPassword());
            if (userEntity != null) {
                String token = jwtProvider.generateToken(String.valueOf(userEntity.getId()));
                return new ResponseEntity<>(new AuthResponse(token), HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/admin/tokenBalance")
    public ResponseEntity<?> changeUserBalance(@RequestBody UserBalanceRequest userBalanceRequest) {
        try {
            userService.changeUserBalance(userBalanceRequest.getId(), userBalanceRequest.getTokenBalance());
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }
}