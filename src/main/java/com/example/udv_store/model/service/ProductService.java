package com.example.udv_store.model.service;

import com.example.udv_store.infrastructure.product.ProductCreationRequest;
import com.example.udv_store.infrastructure.product.ProductResponse;
import com.example.udv_store.model.entity.ProductEntity;
import com.example.udv_store.model.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ProductService {
    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<ProductResponse> getAllProducts() {
        List<ProductEntity> products = productRepository.findAll();
        return products.stream().map(product -> new ProductResponse(product.getId().toString(), product.getName(), product.getPrice())).collect(Collectors.toList());
    }

    public ProductEntity getProduct(UUID id) {
        return productRepository.findByProductId(id);
    }

    public void create(ProductCreationRequest productCreationRequest) {
        ProductEntity product = new ProductEntity();
        product.setName(productCreationRequest.getName());
        product.setPrice(productCreationRequest.getPrice());
        product.setDescription(productCreationRequest.getDescription());
        product.setAmount(productCreationRequest.getAmount());
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
