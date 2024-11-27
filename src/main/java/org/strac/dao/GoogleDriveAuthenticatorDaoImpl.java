package org.strac.dao;

import okhttp3.*;

import java.io.IOException;

public class GoogleDriveAuthenticatorDaoImpl implements GoogleDriveAuthenticatorDao {
    private static final String BASE_URL = "http://localhost:8080/oauth2";
    private static final MediaType JSON_MEDIA_TYPE = MediaType.parse("application/json");
    private final OkHttpClient httpClient;

    public GoogleDriveAuthenticatorDaoImpl() {
        this.httpClient = new OkHttpClient();
    }

    /**
     * Initiates the OAuth 2.0 flow by getting the authorization URL.
     * The frontend should redirect the user to this URL.
     *
     * @return Authorization URL.
     */
    public String getAuthorizationUrl() throws IOException {
        Request request = new Request.Builder()
                .url(BASE_URL + "/auth")
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (response.isSuccessful()) {
                //System.out.println("Response body: " + response.body());
                assert response.body() != null;
                return response.body().string(); // The server sends a redirect URL.
            } else {
                throw new IOException("Failed to get authorization URL: " + response.message());
            }
        }
    }

    /**
     * Handles the callback after the user authorizes access.
     *
     * @param authorizationCode The code returned by Google during the callback.
     * @return JWT token containing the access and refresh tokens.
     */
    public String handleCallback(String authorizationCode) throws IOException {
        HttpUrl.Builder urlBuilder = HttpUrl.parse(BASE_URL + "/callback").newBuilder();
        urlBuilder.addQueryParameter("code", authorizationCode);

        Request request = new Request.Builder()
                .url(urlBuilder.build())
                .get()
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (response.isSuccessful()) {
                return response.body().string();
            } else {
                throw new IOException("Failed to handle callback: " + response.message());
            }
        }
    }

    /**
     * Refreshes the access token using the existing JWT token.
     *
     * @param refreshToken The JWT token containing the refresh token.
     * @return New CredentialsResource with refreshed access and refresh tokens.
     */
    public String refreshAccessToken(String refreshToken) throws IOException {
        Request request = new Request.Builder()
                .url(BASE_URL + "/refresh")
                .post(RequestBody.create("", JSON_MEDIA_TYPE)) // Empty POST body
                .header("Authorization", "Bearer " + refreshToken)
                .build();
        try (Response response = httpClient.newCall(request).execute()) {
            if (response.isSuccessful()) {
                return response.body().string();
            } else {
                throw new IOException("Failed to refresh access token: " + response.message());
            }
        }
    }
}
