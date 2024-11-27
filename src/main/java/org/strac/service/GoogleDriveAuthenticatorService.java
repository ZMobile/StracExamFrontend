package org.strac.service;

import java.awt.*;
import java.net.URI;

public interface GoogleDriveAuthenticatorService {
    boolean isAuthenticated();

    boolean authenticate();

    void unauthenticate();

    void stopLocalServer();

    void refreshToken();
}
