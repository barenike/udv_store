package com.example.udv_store.controller;

import com.example.udv_store.configuration.jwt.JwtProvider;
import com.example.udv_store.exceptions.*;
import com.example.udv_store.infrastructure.user.*;
import com.example.udv_store.model.entity.PasswordResetTokenEntity;
import com.example.udv_store.model.entity.UserEntity;
import com.example.udv_store.model.entity.VerificationTokenEntity;
import com.example.udv_store.model.service.PasswordResetTokenService;
import com.example.udv_store.model.service.UserService;
import com.example.udv_store.model.service.email_verification.OnRegistrationCompleteEvent;
import com.example.udv_store.model.service.email_verification.VerificationTokenService;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureException;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    public UserController(UserService userService,
                          PasswordResetTokenService passwordResetTokenService,
                          VerificationTokenService verificationTokenService,
                          JwtProvider jwtProvider,
                          ApplicationEventPublisher eventPublisher) {
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
        } catch (EmailAlreadyRegisteredException e) {
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
                throw new TokenIsNotFoundException("This verification token doesn't exist.");
            }
            UserEntity user = token.getUser();
            user.setEnabled(true);
            userService.enableUser(user);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (TokenIsNotFoundException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/auth")
    public ResponseEntity<?> auth(@RequestBody @Valid AuthRequest authRequest) {
        try {
            UserEntity user = userService.findByEmailAndPassword(authRequest.getEmail(), authRequest.getPassword());
            if (!user.isEnabled()) {
                throw new NotEnabledUserException("Please, complete registration process by clicking on the link in email we sent you.");
            } else {
                String token = jwtProvider.generateToken(String.valueOf(user.getId()));
                return new ResponseEntity<>(new AuthResponse(token), HttpStatus.OK);
            }
        } catch (IncorrectEmailException | IncorrectPasswordException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (NotEnabledUserException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/info")
    public ResponseEntity<?> getInfo(@RequestHeader(name = "Authorization") String token) {
        try {
            String userId = jwtProvider.getUserIdFromToken(token.substring(7));
            UserEntity user = userService.findByUserId(userId);
            if (userId == null) {
                throw new JSONWebTokenIsNotFoundException("This JWT does not exist.");
            }
            return new ResponseEntity<>(new InfoResponse(
                    user.getId().toString(),
                    user.getRoleEntity().getRoleId(),
                    user.getEmail(),
                    user.getUserBalance()),
                    HttpStatus.OK);
        } catch (JSONWebTokenIsNotFoundException | MalformedJwtException | SignatureException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
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
                throw new UserIsNotFoundByEmailException("There is no account with this email.");
            }
            String appUrl = "http://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath();
            passwordResetTokenService.resetPassword(user, appUrl);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (UserIsNotFoundByEmailException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/reset/password/{token}")
    public ResponseEntity<?> resetPassword(@PathVariable("token") String token,
                                           @RequestBody @Valid ResetPasswordRequest resetPasswordRequest) {
        try {
            PasswordResetTokenEntity passwordResetToken = passwordResetTokenService.validatePasswordResetToken(token);
            if (passwordResetToken == null) {
                throw new TokenIsNotFoundException("This password reset token does not exist.");
            }
            UserEntity user = passwordResetToken.getUser();
            userService.changePassword(user, resetPasswordRequest);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (TokenIsNotFoundException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
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
            UserEntity user = userService.findByUserId(userBalanceRequest.getUserId());
            if (user == null) {
                throw new UserIsNotFoundException("User is not found.");
            }
            userService.changeUserBalance(user, userBalanceRequest.getUserBalance());
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (UserIsNotFoundException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }
}