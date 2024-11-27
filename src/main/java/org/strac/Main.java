package org.strac;

import com.google.gson.Gson;
import org.strac.dao.GoogleDriveDao;
import org.strac.dao.GoogleDriveDaoImpl;
import org.strac.dao.MockGoogleDriveDao;
import org.strac.service.*;
import org.strac.view.GoogleDriveFileViewer;

public class Main {
    public static void main(String[] args) {
        Gson gson = new Gson();
        SimpleTokenStorageService simpleTokenStorageService = new SimpleTokenStorageServiceImpl(gson);
        GoogleDriveDao googleDriveDao = new GoogleDriveDaoImpl(gson);
        GoogleDriveAuthenticatorService googleDriveAuthenticatorService = new GoogleDriveAuthenticatorServiceImpl(simpleTokenStorageService);
        GoogleDriveService googleDriveService = new GoogleDriveServiceImpl(googleDriveDao, googleDriveAuthenticatorService, simpleTokenStorageService);


        GoogleDriveFileViewer googleDriveFileViewer = new GoogleDriveFileViewer(googleDriveService, googleDriveAuthenticatorService);

        googleDriveFileViewer.setVisible(true);
    }
}