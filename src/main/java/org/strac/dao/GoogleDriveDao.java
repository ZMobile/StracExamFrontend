package org.strac.dao;

import okhttp3.HttpUrl;
import okhttp3.Request;
import okhttp3.Response;
import org.strac.model.DriveFile;
import org.strac.model.GoogleDriveFileQueryResponseResource;
import org.strac.model.GoogleDriveResponseResource;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

public interface GoogleDriveDao {
    GoogleDriveFileQueryResponseResource listFiles(DriveFile parent, String authToken);

    GoogleDriveResponseResource uploadFile(String parentId, String authToken, String filePath);

    GoogleDriveResponseResource downloadFile(DriveFile driveFile, String authToken, String destinationPath);

    GoogleDriveResponseResource downloadFolder(DriveFile driveFile, String authToken, String destinationPath);

    GoogleDriveResponseResource deleteFile(String fileId, String authToken);
}
