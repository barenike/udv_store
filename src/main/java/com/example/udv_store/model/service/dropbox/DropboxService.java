package com.example.udv_store.model.service.dropbox;

import com.dropbox.core.DbxException;
import com.dropbox.core.v2.DbxClientV2;
import org.springframework.stereotype.Service;

import java.io.InputStream;

@Service
public class DropboxService {
    private final DbxClientV2 client;

    public DropboxService(DbxClientV2 client) {
        this.client = client;
    }

    public String uploadFile(String filePath, InputStream fileStream) throws DbxException {
        handleDropboxAction(() -> client.files().uploadBuilder(filePath).uploadAndFinish(fileStream),
                String.format("Error uploading file: %s", filePath));
        return client.sharing().createSharedLinkWithSettings(filePath).getUrl();
    }

    public void deleteFile(String filePath) {
        handleDropboxAction(() -> client.files().deleteV2(filePath), String.format("Error deleting file: %s", filePath));
    }

    private <T> void handleDropboxAction(DropboxActionResolver<T> action, String exceptionMessage) {
        try {
            action.perform();
        } catch (Exception e) {
            String messageWithCause = String.format("%s with cause: %s", exceptionMessage, e.getMessage());
            throw new DropboxException(messageWithCause, e);
        }
    }
}
