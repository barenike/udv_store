package com.example.udv_store.model.service;

import com.example.udv_store.model.entity.PasswordResetTokenEntity;
import com.example.udv_store.model.entity.UserEntity;
import com.example.udv_store.model.repository.PasswordResetTokenRepository;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class PasswordResetTokenService {
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final JavaMailSender mailSender;

    public PasswordResetTokenService(PasswordResetTokenRepository passwordResetTokenRepository, JavaMailSender mailSender) {
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.mailSender = mailSender;
    }

    public PasswordResetTokenEntity validatePasswordResetToken(String token) {
        return getToken(token);
    }

    public void createToken(UserEntity user, String token) {
        PasswordResetTokenEntity passwordResetToken = new PasswordResetTokenEntity();
        passwordResetToken.setUser(user);
        passwordResetToken.setToken(token);
        passwordResetTokenRepository.save(passwordResetToken);
    }

    public PasswordResetTokenEntity getToken(final String token) {
        return passwordResetTokenRepository.findByToken(token);
    }

    public void deleteByUserId(UUID userId) {
        PasswordResetTokenEntity token = passwordResetTokenRepository.findByUserId(userId);
        if (token != null) {
            passwordResetTokenRepository.deleteById(token.getId());
        }
    }

    public void resetPassword(UserEntity user, String appUrl) {
        final String token = UUID.randomUUID().toString();
        this.createToken(user, token);
        SimpleMailMessage email = constructEmail(user, token, appUrl);
        mailSender.send(email);
    }

    private SimpleMailMessage constructEmail(UserEntity user, String token, String appUrl) {
        String recipientAddress = user.getEmail();
        String subject = "Reset Password for UDV Store";
        String confirmationUrl = appUrl + "/reset/password/" + token;
        String message = "Please, click on this link if you want to reset your password in UDV Store.";
        SimpleMailMessage email = new SimpleMailMessage();
        email.setFrom("no-reply-udv@mail.ru");
        email.setTo(recipientAddress);
        email.setSubject(subject);
        email.setText(message + " \r\n" + confirmationUrl);
        return email;
    }
}
