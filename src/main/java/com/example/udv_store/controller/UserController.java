package com.example.udv_store.controller;

import com.example.udv_store.configuration.jwt.JwtProvider;
import com.example.udv_store.infrastructure.user.*;
import com.example.udv_store.model.entity.UserEntity;
import com.example.udv_store.model.entity.VerificationTokenEntity;
import com.example.udv_store.model.service.PasswordResetTokenService;
import com.example.udv_store.model.service.UserService;
import com.example.udv_store.model.service.email_verification.OnRegistrationCompleteEvent;
import com.example.udv_store.model.service.email_verification.VerificationTokenService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.UUID;

@RestController
public class UserController {
    private final UserService userService;
    private final PasswordResetTokenService passwordResetTokenService;
    private final VerificationTokenService verificationTokenService;
    private final JwtProvider jwtProvider;
    private final ApplicationEventPublisher eventPublisher;

    public UserController(UserService userService, PasswordResetTokenService passwordResetTokenService, VerificationTokenService verificationTokenService, JwtProvider jwtProvider, ApplicationEventPublisher eventPublisher) {
        this.userService = userService;
        this.passwordResetTokenService = passwordResetTokenService;
        this.verificationTokenService = verificationTokenService;
        this.jwtProvider = jwtProvider;
        this.eventPublisher = eventPublisher;
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody @Valid RegistrationRequest registrationRequest,
                                          HttpServletRequest request) {
        try {
            UserEntity user = userService.create(registrationRequest);
            String appUrl = "http://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath();
            eventPublisher.publishEvent(new OnRegistrationCompleteEvent(user, appUrl));
            return new ResponseEntity<>(HttpStatus.CREATED);
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/register/confirm")
    public ResponseEntity<?> confirmRegistration(@RequestParam("token") String tokenString) {
        try {
            VerificationTokenEntity token = verificationTokenService.getToken(tokenString);
            if (token == null) {
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            }
            UserEntity user = token.getUser();
            user.setEnabled(true);
            userService.enableUser(user);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/auth")
    public ResponseEntity<AuthResponse> auth(@RequestBody @Valid AuthRequest authRequest) {
        try {
            UserEntity user = userService.findByEmailAndPassword(authRequest.getEmail(), authRequest.getPassword());
            if (user == null) {
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            } else if (!user.isEnabled()) {
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            } else {
                String token = jwtProvider.generateToken(String.valueOf(user.getId()));
                return new ResponseEntity<>(new AuthResponse(token), HttpStatus.OK);
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

    @PostMapping("/reset/password/{email}")
    public ResponseEntity<?> sendResetPasswordMail(@PathVariable(name = "email") String email,
                                                   HttpServletRequest request) {
        try {
            UserEntity user = userService.findByEmail(email);
            if (user == null) {
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }
            String appUrl = "http://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath();
            passwordResetTokenService.resetPassword(user, appUrl);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/reset/password/{token}")
    public ResponseEntity<?> resetPassword(@PathVariable("token") String token,
                                           @RequestBody @Valid ResetPasswordRequest resetPasswordRequest) {
        try {
            passwordResetTokenService.validatePasswordResetToken(token);
            UserEntity user = passwordResetTokenService.getToken(token).getUser();
            userService.changePassword(user, resetPasswordRequest);
            return new ResponseEntity<>(HttpStatus.OK);
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