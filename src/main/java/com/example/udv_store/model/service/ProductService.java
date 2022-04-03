package com.example.udv_store.model.service;

import com.example.udv_store.model.entity.ProductEntity;
import com.example.udv_store.model.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ProductService {
    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<ProductEntity> getAllProducts() {
        return productRepository.findAll();
    }

    public ProductEntity getProduct(UUID productId) {
        return productRepository.findByProductId(productId);
    }

    public void create(ProductEntity product) {
        productRepository.save(product);
    }

    public boolean delete(UUID id) {
        if (productRepository.existsById(id)) {
            productRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
