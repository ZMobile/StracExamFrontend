package org.strac.model;

public class GoogleDriveResponseResource {
    private boolean success;
    private int code;

    public GoogleDriveResponseResource(boolean success, int code) {
        this.success = success;
        this.code = code;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }
}
