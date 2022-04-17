package com.example.udv_store.controller;

import com.example.udv_store.configuration.jwt.JwtProvider;
import com.example.udv_store.infrastructure.user.*;
import com.example.udv_store.model.entity.UserEntity;
import com.example.udv_store.model.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.UUID;

@RestController
public class UserController {
    private final UserService userService;
    private final JwtProvider jwtProvider;

    public UserController(UserService userService, JwtProvider jwtProvider) {
        this.userService = userService;
        this.jwtProvider = jwtProvider;
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody @Valid RegistrationRequest registrationRequest) {
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
    public ResponseEntity<AuthResponse> auth(@RequestBody @Valid AuthRequest authRequest) {
        try {
            UserEntity user = userService.findByEmailAndPassword(authRequest.getEmail(), authRequest.getPassword());
            if (user != null) {
                String token = jwtProvider.generateToken(String.valueOf(user.getId()));
                return new ResponseEntity<>(new AuthResponse(token), HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/info")
    public ResponseEntity<InfoResponse> getInfo(@RequestHeader(name = "Authorization") String token) {
        try {
            String userId = jwtProvider.getUserIdFromToken(token.substring(7));
            UserEntity user = userService.findByUserId(userId);
            return new ResponseEntity<>(new InfoResponse(
                    user.getId().toString(),
                    user.getRoleEntity().getRoleId(),
                    user.getEmail(),
                    user.getUserBalance()),
                    HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/admin/user/{userId}")
    public ResponseEntity<?> deleteUser(@PathVariable(name = "userId") UUID userId) {
        try {
            final boolean isDeleted = userService.delete(userId);
            return isDeleted
                    ? new ResponseEntity<>(HttpStatus.OK)
                    : new ResponseEntity<>(HttpStatus.NOT_MODIFIED);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/admin/user_balance")
    public ResponseEntity<?> changeUserBalance(@RequestBody @Valid UserBalanceRequest userBalanceRequest) {
        try {
            userService.changeUserBalance(userBalanceRequest.getUserId(), userBalanceRequest.getUserBalance());
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }
}