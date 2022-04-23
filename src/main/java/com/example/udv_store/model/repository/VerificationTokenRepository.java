package com.example.udv_store.model.repository;

import com.example.udv_store.model.entity.VerificationTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface VerificationTokenRepository extends JpaRepository<VerificationTokenEntity, UUID> {
    VerificationTokenEntity findByToken(String token);

    VerificationTokenEntity findByUserId(UUID userId);
}
