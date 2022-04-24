package com.example.udv_store.exceptions;

public class ProductIsNotFoundException extends RuntimeException {
    public ProductIsNotFoundException(String message) {
        super(message);
    }
}
