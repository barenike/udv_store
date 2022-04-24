package com.example.udv_store.exceptions;

public class ImageDeleteInDropboxFailedException extends RuntimeException {
    public ImageDeleteInDropboxFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}
