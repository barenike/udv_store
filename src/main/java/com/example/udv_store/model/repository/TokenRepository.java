package com.example.udv_store.model.repository;

import com.example.udv_store.model.entity.TokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TokenRepository extends JpaRepository<TokenEntity, UUID> {
    TokenEntity findByToken(String token);

    TokenEntity findByUserId(UUID userId);
}
