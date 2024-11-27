package org.strac.service;

import org.strac.dao.GoogleDriveDao;
import org.strac.model.DriveFile;
import org.strac.model.GoogleDriveFileQueryResponseResource;

import java.util.List;

public class GoogleDriveServiceImpl implements GoogleDriveService {
    private final GoogleDriveDao driveDao;
    private final GoogleDriveAuthenticatorService driveAuthenticatorService;
    private final SimpleTokenStorageService simpleTokenStorageService;
    private final MimeTypeToExtensionTransformerService mimeTypeToExtensionTransformerService;

    public GoogleDriveServiceImpl(GoogleDriveDao driveDao,
                                  GoogleDriveAuthenticatorService driveAuthenticatorService,
                                  SimpleTokenStorageService simpleTokenStorageService) {
        this.driveDao = driveDao;
        this.driveAuthenticatorService = driveAuthenticatorService;
        this.simpleTokenStorageService = simpleTokenStorageService;
        this.mimeTypeToExtensionTransformerService = new MimeTypeToExtensionTransformerServiceImpl();
    }

    public List<DriveFile> listFiles(String parentId) {
        String accessToken = simpleTokenStorageService.loadCredentials().getAccessToken();
        GoogleDriveFileQueryResponseResource googleDriveFileQueryResponseResource = driveDao.listFiles(parentId, accessToken);
        if (googleDriveFileQueryResponseResource.getGoogleDriveResponseResource().isSuccess()) {
            for (DriveFile file : googleDriveFileQueryResponseResource.getFiles()) {
                file.setExtension(mimeTypeToExtensionTransformerService.getFileExtensionFromMimeType(file.getMimeType()));
            }
            return googleDriveFileQueryResponseResource.getFiles();
        } else if (googleDriveFileQueryResponseResource.getGoogleDriveResponseResource().getCode() == 403) {
            driveAuthenticatorService.refreshToken();
            accessToken = simpleTokenStorageService.loadCredentials().getAccessToken();
            googleDriveFileQueryResponseResource = driveDao.listFiles(parentId, accessToken);
            if (googleDriveFileQueryResponseResource.getGoogleDriveResponseResource().isSuccess()) {
                return googleDriveFileQueryResponseResource.getFiles();
            } else {
                throw new RuntimeException("Error listing files");
            }
        }
        throw new RuntimeException("Error listing files");
    }

    public void uploadFile(String parentId, String filePath) {
        String accessToken = simpleTokenStorageService.loadCredentials().getAccessToken();
        driveDao.uploadFile(parentId, accessToken, filePath);
    }

    public void downloadFile(DriveFile driveFile, String destinationPath) {
        String accessToken = simpleTokenStorageService.loadCredentials().getAccessToken();
        driveDao.downloadFile(driveFile, accessToken, destinationPath);
    }

    public void downloadFolder(DriveFile driveFile, String destinationPath) {
        String accessToken = simpleTokenStorageService.loadCredentials().getAccessToken();
        driveDao.downloadFolder(driveFile, accessToken, destinationPath);
    }

    public void deleteFile(String fileId) {
        String accessToken = simpleTokenStorageService.loadCredentials().getAccessToken();
        driveDao.deleteFile(fileId, accessToken);
    }
}
