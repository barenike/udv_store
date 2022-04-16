package com.example.udv_store.model.service.dropbox;

@FunctionalInterface
interface DropboxActionResolver<T> {
    T perform() throws Exception;
}
