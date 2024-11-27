package org.strac.service;

import org.strac.model.CredentialsResource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public interface SimpleTokenStorageService {
    /**
     * Saves the credentials to a file.
     *
     * @param credentials The CredentialsResource to save.
     */
    void saveCredentials(CredentialsResource credentials);

    /**
     * Saves the credentials to a file.
     *
     * @param credentialsJson The CredentialsResource json data to save.
     */
    void saveCredentialsJson(String credentialsJson);

    /**
     * Loads the credentials from the file.
     *
     * @return The loaded CredentialsResource, or null if no credentials exist.
     */
    CredentialsResource loadCredentials();

    /**
     * Deletes the stored credentials.
     */
    void clearCredentials();
}
