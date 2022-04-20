package com.example.udv_store.model.service.email_verification;

import com.example.udv_store.model.entity.TokenEntity;
import com.example.udv_store.model.entity.UserEntity;
import com.example.udv_store.model.repository.TokenRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class TokenService {
    private final TokenRepository tokenRepository;

    public TokenService(TokenRepository tokenRepository) {
        this.tokenRepository = tokenRepository;
    }

    public void createToken(UserEntity user, String token) {
        TokenEntity myToken = new TokenEntity();
        myToken.setUser(user);
        myToken.setToken(token);
        tokenRepository.save(myToken);
    }

    public TokenEntity getToken(final String VerificationToken) {
        return tokenRepository.findByToken(VerificationToken);
    }

    public boolean deleteByUserId(UUID userId) {
        TokenEntity token = tokenRepository.findByUserId(userId);
        if (token != null) {
            tokenRepository.deleteById(token.getId());
            return true;
        }
        return false;
    }
}
