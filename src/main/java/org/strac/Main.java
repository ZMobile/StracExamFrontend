package org.strac;

import com.google.gson.Gson;
import org.strac.dao.GoogleDriveDao;
import org.strac.dao.GoogleDriveDaoImpl;
import org.strac.service.*;
import org.strac.view.GoogleDriveFileViewer;

public class Main {
    private static String BACKEND_BASE_URL ="http://localhost:8080";

    public static void main(String[] args) {
        Gson gson = new Gson();
        SimpleTokenStorageService simpleTokenStorageService = new SimpleTokenStorageServiceImpl(gson);
        GoogleDriveDao googleDriveDao = new GoogleDriveDaoImpl(BACKEND_BASE_URL, gson);
        GoogleDriveAuthenticatorService googleDriveAuthenticatorService = new GoogleDriveAuthenticatorServiceImpl(BACKEND_BASE_URL, simpleTokenStorageService);
        GoogleDriveService googleDriveService = new GoogleDriveServiceImpl(googleDriveDao, googleDriveAuthenticatorService, simpleTokenStorageService);


        GoogleDriveFileViewer googleDriveFileViewer = new GoogleDriveFileViewer(googleDriveService, googleDriveAuthenticatorService);

        googleDriveFileViewer.setVisible(true);
    }
}