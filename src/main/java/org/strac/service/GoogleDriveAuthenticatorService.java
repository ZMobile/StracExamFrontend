package org.strac.service;

import java.awt.*;
import java.net.URI;

public interface GoogleDriveAuthenticatorService {
    /**
     * Check if the user is authenticated.
     * @return True if the user is authenticated, false otherwise.
     */
    boolean isAuthenticated();

    /**
     * Authenticate the user.
     * @return True if the user is successfully authenticated, false otherwise.
     */
    boolean authenticate();

    /**
     * Remove the user's authentication.
     */
    void unauthenticate();

    /**
     * Stop the local server which listens for the redirect.
     */
    void stopLocalServer();

    /**
     * Refresh the access token.
     */
    void refreshToken();
}
