package com.example.udv_store.exceptions;

public class OrderRecordIsNotFoundException extends RuntimeException {
    public OrderRecordIsNotFoundException(String message) {
        super(message);
    }
}
