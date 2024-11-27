package org.strac.dao;

import org.strac.model.DriveFile;
import org.strac.model.GoogleDriveFileQueryResponseResource;
import org.strac.model.GoogleDriveResponseResource;

import java.util.List;

public interface GoogleDriveDao {
    GoogleDriveFileQueryResponseResource listFiles(String parentId, String authToken);

    GoogleDriveResponseResource uploadFile(String parentId, String filePath, String authToken);

    GoogleDriveResponseResource downloadFile(String fileId, String destinationPath, String authToken);

    GoogleDriveResponseResource downloadFolder(String folderId, String destinationPath, String authToken);

    GoogleDriveResponseResource deleteFile(String fileId, String authToken);
}
