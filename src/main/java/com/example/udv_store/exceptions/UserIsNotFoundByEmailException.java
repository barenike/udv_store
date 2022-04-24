package com.example.udv_store.exceptions;

public class UserIsNotFoundByEmailException extends RuntimeException {
    public UserIsNotFoundByEmailException(String message) {
        super(message);
    }
}
