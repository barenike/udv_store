package com.example.udv_store.model.service;

import com.example.udv_store.infrastructure.product.CreateProductRequest;
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

    public void create(CreateProductRequest createProductRequest) {
        ProductEntity product = new ProductEntity();
        product.setName(createProductRequest.getName());
        product.setPrice(createProductRequest.getPrice());
        product.setDescription(createProductRequest.getDescription());
        product.setAmount(createProductRequest.getAmount());
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
