package com.example.udv_store.controller;

import com.example.udv_store.configuration.jwt.JwtProvider;
import com.example.udv_store.infrastructure.user.AuthRequest;
import com.example.udv_store.infrastructure.user.AuthResponse;
import com.example.udv_store.infrastructure.user.RegistrationRequest;
import com.example.udv_store.infrastructure.user.TokenBalanceRequest;
import com.example.udv_store.model.entity.UserEntity;
import com.example.udv_store.model.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
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
            UserEntity user = new UserEntity();
            user.setEmail(registrationRequest.getEmail());
            user.setPassword(registrationRequest.getPassword());
            user.setTokenBalance(0);
            userService.create(user);
            return new ResponseEntity<>(HttpStatus.CREATED);
        } catch (AccessDeniedException e) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
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

    // Doesn't work! Change the logic of controller or the logic of generateToken.
    @PostMapping("/exit")
    public ResponseEntity<?> exit(@RequestHeader(name = "Authorization") String token) {
        try {
            String userId = jwtProvider.getUserIdFromToken(token.substring(7));
            jwtProvider.generateToken(userId);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/admin/tokenBalance")
    public ResponseEntity<?> changeTokenBalance(@RequestBody TokenBalanceRequest tokenBalanceRequest) {
        try {
            userService.changeTokenBalance(tokenBalanceRequest.getId(), tokenBalanceRequest.getTokenBalance());
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }
}