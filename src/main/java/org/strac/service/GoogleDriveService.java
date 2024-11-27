package org.strac.service;

import org.strac.model.DriveFile;

import java.util.List;

public interface GoogleDriveService {
    List<DriveFile> listFiles(String parentId);

    void uploadFile(String parentId, String filePath);

    void downloadFile(DriveFile driveFile, String destinationPath);

    void downloadFolder(DriveFile driveFile, String destinationPath);

    void deleteFile(String fileId);
}
