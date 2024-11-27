package org.strac.dao;

import okhttp3.HttpUrl;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;

public interface GoogleDriveAuthenticatorDao {
    /**
     * Initiates the OAuth 2.0 flow by getting the authorization URL.
     * The frontend should redirect the user to this URL.
     *
     * @return Authorization URL.
     */
    String getAuthorizationUrl() throws IOException;

    /**
     * Handles the callback after the user authorizes access.
     *
     * @param authorizationCode The code returned by Google during the callback.
     * @return JWT token containing the access and refresh tokens.
     */
    String handleCallback(String authorizationCode) throws IOException;

    /**
     * Refreshes the access token using the existing JWT token.
     *
     * @param jwtToken The JWT token containing the refresh token.
     * @return New JWT token with refreshed access and refresh tokens.
     */
    public String refreshAccessToken(String jwtToken) throws IOException;
}
