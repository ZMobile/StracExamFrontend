package org.strac.service;

import org.strac.model.DriveFile;

import java.util.List;

public interface GoogleDriveService {
    List<DriveFile> listFiles(String parentId);

    void uploadFile(String parentId, String filePath);

    void downloadFile(String fileId, String destinationPath);

    void downloadFolder(String fileId, String destinationPath);

    void deleteFile(String fileId);
}
