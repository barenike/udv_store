package com.example.udv_store.model.service.email_verification;

import com.example.udv_store.model.entity.UserEntity;
import org.springframework.context.ApplicationListener;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class RegistrationListener implements ApplicationListener<OnRegistrationCompleteEvent> {
    private final VerificationTokenService verificationTokenService;
    private final JavaMailSender mailSender;

    public RegistrationListener(VerificationTokenService verificationTokenService, JavaMailSender mailSender) {
        this.verificationTokenService = verificationTokenService;
        this.mailSender = mailSender;
    }

    @Override
    public void onApplicationEvent(OnRegistrationCompleteEvent event) {
        this.confirmRegistration(event);
    }

    private void confirmRegistration(final OnRegistrationCompleteEvent event) {
        UserEntity user = event.getUser();
        final String token = UUID.randomUUID().toString();
        verificationTokenService.createToken(user, token);
        SimpleMailMessage email = constructEmailMessage(event, user, token);
        mailSender.send(email);
    }

    private SimpleMailMessage constructEmailMessage(OnRegistrationCompleteEvent event, UserEntity user, String token) {
        String recipientAddress = user.getEmail();
        String subject = "Registration Confirmation for UDV Store";
        String confirmationUrl = event.getAppUrl() + "/register/confirm?token=" + token;
        String message = "Please, click on this link if you want to complete registration in UDV Store.";
        SimpleMailMessage email = new SimpleMailMessage();
        email.setFrom("no-reply-udv@mail.ru");
        email.setTo(recipientAddress);
        email.setSubject(subject);
        email.setText(message + " \r\n" + confirmationUrl);
        return email;
    }
}
