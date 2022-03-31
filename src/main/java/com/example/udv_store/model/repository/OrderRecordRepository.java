package com.example.udv_store.model.repository;

import com.example.udv_store.model.entity.OrderRecordEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface OrderRecordRepository extends JpaRepository<OrderRecordEntity, UUID> {
}
