package org.strac.model;

import java.util.List;

public class GoogleDriveFileQueryResponseResource {
    private GoogleDriveResponseResource googleDriveResponseResource;
    private List<DriveFile> files;

    public GoogleDriveFileQueryResponseResource(boolean success, int code, List<DriveFile> files) {
        this.googleDriveResponseResource = new GoogleDriveResponseResource(success, code);
        this.files = files;
    }

    public GoogleDriveResponseResource getGoogleDriveResponseResource() {
        return googleDriveResponseResource;
    }

    public void setGoogleDriveResponseResource(GoogleDriveResponseResource googleDriveResponseResource) {
        this.googleDriveResponseResource = googleDriveResponseResource;
    }

    public List<DriveFile> getFiles() {
        return files;
    }

    public void setFiles(List<DriveFile> files) {
        this.files = files;
    }
}
