package org.strac.model;

public class DriveFile {
    private String id;
    private String name;
    private String mimeType;
    private String parentId;
    private String path;
    private String extension;

    // Constructor
    public DriveFile(String id, String name, String mimeType, String parentId, String path) {
        this.id = id;
        this.name = name;
        this.mimeType = mimeType;
        this.parentId = parentId;
        this.path = path;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    @Override
    public String toString() {
        return name;
    }
}
