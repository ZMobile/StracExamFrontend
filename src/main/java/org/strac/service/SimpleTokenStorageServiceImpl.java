package org.strac.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.strac.model.CredentialsResource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class SimpleTokenStorageServiceImpl implements SimpleTokenStorageService {
    private static final String STORAGE_FILE = System.getProperty("user.home") + "/.google_drive_auth_credentials";
    private final Gson gson;

    private CredentialsResource credentials;

    public SimpleTokenStorageServiceImpl(Gson gson) {
        this.gson = gson;
        this.credentials = loadCredentials();
    }

    /**
     * Saves the credentials to a file.
     *
     * @param credentials The CredentialsResource to save.
     */
    public void saveCredentials(CredentialsResource credentials) {
        String json = gson.toJson(credentials);
        saveCredentialsJson(json);
    }

    /**
     * Saves the credentials to a file.
     *
     * @param credentialsJson The CredentialsResource json data to save.
     */
    public void saveCredentialsJson(String credentialsJson) {
        try {
            Files.writeString(Path.of(STORAGE_FILE), credentialsJson, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            this.credentials = gson.fromJson(credentialsJson, CredentialsResource.class);
        } catch (IOException e) {
            throw new RuntimeException("Error saving credentials", e);
        }
    }

    /**
     * Loads the credentials from the file.
     *
     * @return The loaded CredentialsResource, or null if no credentials exist.
     */
    public CredentialsResource loadCredentials() {
        if (credentials != null) {
            return credentials;
        }
        try {
            if (Files.exists(Path.of(STORAGE_FILE))) {
                String json = Files.readString(Path.of(STORAGE_FILE));
                return gson.fromJson(json, CredentialsResource.class);
            } else {
                return null;
            }
        } catch (IOException e) {
            throw new RuntimeException("Error loading credentials", e);
        }
    }

    /**
     * Deletes the stored credentials.
     */
    public void clearCredentials() {
        try {
            Files.deleteIfExists(Path.of(STORAGE_FILE));
            this.credentials = null;
        } catch (IOException e) {
            throw new RuntimeException("Error clearing credentials", e);
        }
    }
}
