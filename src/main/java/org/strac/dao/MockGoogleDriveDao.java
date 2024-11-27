package org.strac.dao;

import org.strac.model.DriveFile;
import org.strac.model.GoogleDriveFileQueryResponseResource;
import org.strac.model.GoogleDriveResponseResource;

import java.util.ArrayList;
import java.util.List;

public class MockGoogleDriveDao implements GoogleDriveDao {
    private final List<DriveFile> files;

    public MockGoogleDriveDao() {
        files = new ArrayList<>();
        initializeMockData();
    }

    private void initializeMockData() {
        // Root folder
        files.add(new DriveFile("root", "Root Folder", "application/vnd.google-apps.folder", null, "Root Folder"));

        // Subfolders in root
        files.add(new DriveFile("folder1", "Documents", "application/vnd.google-apps.folder", "root", "Root Folder/Documents"));
        files.add(new DriveFile("folder2", "Images", "application/vnd.google-apps.folder", "root", "Root Folder/Images"));

        // Files in Documents folder
        files.add(new DriveFile("file1", "Resume.pdf", "application/pdf", "folder1", "Root Folder/Documents/Resume.pdf"));
        files.add(new DriveFile("file2", "ProjectPlan.docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document", "folder1", "Root Folder/Documents/ProjectPlan.docx"));

        // Files in Images folder
        files.add(new DriveFile("file3", "VacationPhoto.jpg", "image/jpeg", "folder2", "Root Folder/Images/VacationPhoto.jpg"));
        files.add(new DriveFile("file4", "Logo.png", "image/png", "folder2", "Root Folder/Images/Logo.png"));
    }

    @Override
    public GoogleDriveFileQueryResponseResource listFiles(String parentId, String authToken) {
        List<DriveFile> result = new ArrayList<>();
        for (DriveFile file : files) {
            if (parentId.equals(file.getParentId())) {
                result.add(file);
            }
        }
        return new GoogleDriveFileQueryResponseResource(true, 200, result);
    }

    @Override
    public GoogleDriveResponseResource uploadFile(String parentId, String filePath, String authToken) {
        System.out.println("Simulating upload of file: " + filePath + " to folder: " + parentId);
        return new GoogleDriveResponseResource(true, 200);
    }

    @Override
    public GoogleDriveResponseResource downloadFile(String fileId, String destinationPath, String authToken) {
        System.out.println("Simulating download of file ID: " + fileId + " to destination: " + destinationPath);
        return new GoogleDriveResponseResource(true, 200);
    }

    @Override
    public GoogleDriveResponseResource downloadFolder(String folderId, String destinationPath, String authToken) {
        return new GoogleDriveResponseResource(true, 200);
    }

    @Override
    public GoogleDriveResponseResource deleteFile(String fileId, String authToken) {
        System.out.println("Simulating deletion of file ID: " + fileId);
        files.removeIf(file -> file.getId().equals(fileId));
        return new GoogleDriveResponseResource(true, 200);
    }
}
