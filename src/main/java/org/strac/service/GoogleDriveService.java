package org.strac.service;

import org.strac.model.DriveFile;

import java.util.List;

public interface GoogleDriveService {
    /**
        * List files in a folder in the Google Drive.
        *
        * @param parent The parent folder to list files from.
        * @return The list of files in the folder.
    */
    List<DriveFile> listFiles(DriveFile parent);

    /**
     * Upload a file to the Google Drive.
     *
     * @param parentId The ID of the parent folder.
     * @param filePath The path of the file to upload.
     */
    void uploadFile(String parentId, String filePath);

    /**
     * Download a file from the Google Drive.
     *
     * @param driveFile The file to download.
     * @param destinationPath The path to save the downloaded file.
     */
    void downloadFile(DriveFile driveFile, String destinationPath);

    /**
     * Download a folder from the Google Drive.
     *
     * @param driveFile The folder to download.
     * @param destinationPath The path to save the downloaded folder.
     */
    void downloadFolder(DriveFile driveFile, String destinationPath);

    /**
     * Delete a file from the Google Drive.
     *
     * @param fileId The ID of the file to delete.
     */
    void deleteFile(String fileId);
}
