package com.example.udv_store.controller;

import com.example.udv_store.infrastructure.product.ProductAmountRequest;
import com.example.udv_store.infrastructure.product.ProductCreationRequest;
import com.example.udv_store.infrastructure.product.ProductResponse;
import com.example.udv_store.model.entity.ProductEntity;
import com.example.udv_store.model.service.ProductService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.UUID;

@RestController
public class ProductController {
    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping("/admin/product")
    public ResponseEntity<?> createProduct(@ModelAttribute @Valid ProductCreationRequest productCreationRequest) {
        try {
            productService.create(productCreationRequest);
            return new ResponseEntity<>(HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/admin/product_amount")
    public ResponseEntity<?> changeProductAmount(@RequestBody @Valid ProductAmountRequest productAmountRequest) {
        try {
            productService.changeProductAmount(productAmountRequest.getProductId(), productAmountRequest.getAmount());
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/products")
    public ResponseEntity<List<ProductResponse>> getProducts() {
        try {
            final List<ProductResponse> products = productService.getAllProducts();
            return products != null && !products.isEmpty()
                    ? new ResponseEntity<>(products, HttpStatus.OK)
                    : new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/products/{productId}")
    public ResponseEntity<ProductEntity> getProduct(@PathVariable(name = "productId") UUID productId) {
        try {
            final ProductEntity product = productService.getProduct(productId);
            return product != null
                    ? new ResponseEntity<>(product, HttpStatus.OK)
                    : new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/admin/product/{productId}")
    public ResponseEntity<?> deleteProduct(@PathVariable(name = "productId") UUID productId) {
        try {
            final boolean isDeleted = productService.delete(productId);
            return isDeleted
                    ? new ResponseEntity<>(HttpStatus.OK)
                    : new ResponseEntity<>(HttpStatus.NOT_MODIFIED);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }
}
