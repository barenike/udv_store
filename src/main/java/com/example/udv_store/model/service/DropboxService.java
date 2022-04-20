package com.example.udv_store.model.service;

import com.dropbox.core.DbxException;
import com.dropbox.core.v2.DbxClientV2;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;

@Service
public class DropboxService {
    private final DbxClientV2 client;

    public DropboxService(DbxClientV2 client) {
        this.client = client;
    }

    public String upload(String path, InputStream inputStream) throws IOException, DbxException {
        client.files().uploadBuilder(path).uploadAndFinish(inputStream);
        return client.sharing().createSharedLinkWithSettings(path).getUrl();
    }

    public void delete(String filePath) throws DbxException {
        client.files().deleteV2(filePath);
    }
}
