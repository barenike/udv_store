package com.example.udv_store.exceptions;

public class ImageUploadToDropboxFailedException extends RuntimeException {
    public ImageUploadToDropboxFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}
