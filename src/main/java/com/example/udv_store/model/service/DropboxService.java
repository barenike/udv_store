package com.example.udv_store.model.service;

import com.dropbox.core.DbxException;
import com.dropbox.core.v2.DbxClientV2;
import com.example.udv_store.exceptions.ImageDeleteInDropboxFailedException;
import com.example.udv_store.exceptions.ImageUploadToDropboxFailedException;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;

@Service
public class DropboxService {
    private final DbxClientV2 client;

    public DropboxService(DbxClientV2 client) {
        this.client = client;
    }

    public String upload(String path, InputStream inputStream) {
        try {
            client.files().uploadBuilder(path).uploadAndFinish(inputStream);
            return client.sharing().createSharedLinkWithSettings(path).getUrl();
        } catch (IOException | DbxException e) {
            throw new ImageUploadToDropboxFailedException("Image upload to Dropbox failed.", e);
        }
    }

    public void delete(String filePath) {
        try {
            client.files().deleteV2(filePath);
        } catch (DbxException e) {
            throw new ImageDeleteInDropboxFailedException("Failed to delete image in Dropbox.", e);
        }
    }
}
