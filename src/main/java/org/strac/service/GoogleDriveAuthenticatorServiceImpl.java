package org.strac.service;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpServer;
import org.strac.dao.GoogleDriveAuthenticatorDao;
import org.strac.dao.GoogleDriveAuthenticatorDaoImpl;
import org.strac.model.CredentialsResource;

import javax.swing.*;
import java.awt.*;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GoogleDriveAuthenticatorServiceImpl implements GoogleDriveAuthenticatorService {
    private static final int REDIRECT_PORT = 8081;
    private static final String REDIRECT_ENDPOINT = "/oauth2/callback";

    private final GoogleDriveAuthenticatorDao dao;
    private final SimpleTokenStorageService tokenStorage;
    private HttpServer server;
    private ExecutorService executor;

    public GoogleDriveAuthenticatorServiceImpl(String BASE_URL, SimpleTokenStorageService simpleTokenStorageService) {
        this.dao = new GoogleDriveAuthenticatorDaoImpl(BASE_URL);
        this.tokenStorage = simpleTokenStorageService;
    }

    public boolean isAuthenticated() {
        return tokenStorage.loadCredentials() != null;
    }

    public boolean authenticate() {
        try {
            // Get the authorization URL
            String authUrl = dao.getAuthorizationUrl();
            // Open the URL in the default web browser
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(new URI(authUrl));
            } else {
                JOptionPane.showMessageDialog(null, "Failed to open the browser. Please open the following URL manually: " + authUrl);
            }

            // Start the local server to listen for the redirect
            startLocalServer();

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public void unauthenticate() {
        tokenStorage.clearCredentials(); // Delete the stored tokens
    }

    private void startLocalServer() {
        try {
            if (server != null) {
                System.out.println("Server is already running.");
                return;
            }

            server = HttpServer.create(new InetSocketAddress(REDIRECT_PORT), 0); // The server port should match the redirect url.
            executor = Executors.newSingleThreadExecutor();

            server.createContext(REDIRECT_ENDPOINT, exchange -> {
                String query = exchange.getRequestURI().getQuery();
                String code = null;

                if (query != null && query.contains("code=")) {
                    code = query.split("code=")[1].split("&")[0];
                }

                String responseMessage;
                if (code != null) {
                    try {
                        // Handle the callback and fetch the JWT token
                        String jsonData = dao.handleCallback(code);
                        tokenStorage.saveCredentialsJson(jsonData);
                        responseMessage = "Authentication completed successfully. You may close this browser.";
                    } catch (Exception e) {
                        e.printStackTrace();
                        responseMessage = "Failed to authenticate. Please try again.";
                    }
                } else {
                    responseMessage = "Invalid callback. No authorization code found.";
                }

                exchange.sendResponseHeaders(200, responseMessage.getBytes().length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(responseMessage.getBytes());
                } finally {
                    stopLocalServer();
                }

            });

            server.setExecutor(executor);
            server.start();
            System.out.println("Local server started. Listening for OAuth 2.0 redirect.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stopLocalServer() {
        if (server != null) {
            server.stop(0);
            executor.shutdown();
            server = null;
            System.out.println("Local server stopped.");
        }
    }

    public void refreshToken() {
        CredentialsResource credentials = tokenStorage.loadCredentials();
        if (credentials == null) {
            throw new IllegalStateException("No credentials found. Please authenticate first.");
        }

        try {
            String newCredentials = dao.refreshAccessToken(credentials.getRefreshToken());
            tokenStorage.saveCredentialsJson(newCredentials);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to refresh the access token.", e);
        }
    }
}
