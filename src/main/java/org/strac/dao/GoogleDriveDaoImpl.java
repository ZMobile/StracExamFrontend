package org.strac.dao;

import okhttp3.*;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.strac.model.DriveFile;
import org.strac.model.GoogleDriveFileQueryResponseResource;
import org.strac.model.GoogleDriveResponseResource;
import org.strac.service.SimpleTokenStorageService;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Objects;

public class GoogleDriveDaoImpl implements GoogleDriveDao {
    private final String URL;
    private final OkHttpClient httpClient;
    private final Gson gson;

    public GoogleDriveDaoImpl(String BASE_URL, Gson gson) {
        String ENDPOINT = "/api/drive";
        this.URL = BASE_URL + ENDPOINT;
        this.httpClient = new OkHttpClient();
        this.gson = gson;
    }

    private Request.Builder createRequestBuilder(HttpUrl url, String authToken) {
        if (authToken == null || authToken.isEmpty()) {
            throw new IllegalArgumentException("Authorization token cannot be null or empty.");
        }
        return new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + authToken);
    }

    @Override
    public GoogleDriveFileQueryResponseResource listFiles(String parentId, String authToken) {
        HttpUrl.Builder urlBuilder = HttpUrl.parse(URL + "/files").newBuilder();
        if (parentId != null && !parentId.isEmpty()) {
            urlBuilder.addQueryParameter("parentId", parentId);
        }

        Request request = createRequestBuilder(urlBuilder.build(), authToken)
                .get()
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                Type listType = new TypeToken<List<DriveFile>>() {}.getType();
                List<DriveFile> files = gson.fromJson(response.body().string(), listType);
                return new GoogleDriveFileQueryResponseResource(true, response.code(), files);
            } else {
                return new GoogleDriveFileQueryResponseResource(false, response.code(), null);
            }
        } catch (IOException e) {
            throw new RuntimeException("Error listing files", e);
        }
    }

    @Override
    public GoogleDriveResponseResource uploadFile(String parentId, String authToken, String filePath) {
        File file = new File(filePath);
        if (!file.exists()) {
            throw new IllegalArgumentException("File does not exist: " + filePath);
        }

        HttpUrl.Builder urlBuilder = HttpUrl.parse(URL + "/upload").newBuilder();
        if (parentId != null && !parentId.isEmpty()) {
            urlBuilder.addQueryParameter("folderId", parentId);
        }

        RequestBody fileBody = RequestBody.create(file, MediaType.parse("application/octet-stream"));
        MultipartBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", file.getName(), fileBody)
                .build();

        Request request = createRequestBuilder(urlBuilder.build(), authToken)
                .post(requestBody)
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (response.isSuccessful()) {
                return new GoogleDriveResponseResource(true, response.code());
            } else {
                return new GoogleDriveResponseResource(false, response.code());
            }
        } catch (IOException e) {
            throw new RuntimeException("Error uploading file", e);
        }
    }

    @Override
    public GoogleDriveResponseResource downloadFile(DriveFile driveFile, String authToken, String destinationPath) {
        HttpUrl.Builder urlBuilder = Objects.requireNonNull(HttpUrl.parse(URL + "/download/file")).newBuilder()
                .addQueryParameter("fileId", driveFile.getId());

        Request request = createRequestBuilder(urlBuilder.build(), authToken)
                .get()
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                byte[] fileBytes = response.body().bytes();

                String extension = driveFile.getExtension();

                // Append extension if not already present
                String fileName = driveFile.getName();
                if (!fileName.endsWith(extension)) {
                    fileName += extension;
                }

                File destinationFile = new File(destinationPath, fileName);
                System.out.println("Destination path: " + destinationPath);
                if (destinationFile.isDirectory()) {
                    throw new IllegalArgumentException("Destination path must include a file name, not just a directory.");
                }
                // Ensure the parent directories exist
                File parentDir = destinationFile.getParentFile();
                if (parentDir != null && !parentDir.exists()) {
                    boolean dirsCreated = parentDir.mkdirs();
                    if (!dirsCreated) {
                        throw new IOException("Failed to create parent directories for destination path.");
                    }
                }

                // Save the file to the specified destination path
                try (FileOutputStream fos = new FileOutputStream(destinationFile)) {
                    fos.write(fileBytes);
                }
                return new GoogleDriveResponseResource(true, response.code());
            } else {
                return new GoogleDriveResponseResource(false, response.code());
            }
        } catch (IOException e) {
            throw new RuntimeException("Error downloading file", e);
        }
    }

    @Override
    public GoogleDriveResponseResource downloadFolder(DriveFile driveFile, String authToken, String destinationPath) {
        HttpUrl.Builder urlBuilder = Objects.requireNonNull(HttpUrl.parse(URL + "/download/folder")).newBuilder()
                .addQueryParameter("folderId", driveFile.getId());

        Request request = createRequestBuilder(urlBuilder.build(), authToken)
                .get()
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                byte[] folderBytes = response.body().bytes();

                if (destinationPath == null || destinationPath.isEmpty()) {
                    throw new IllegalArgumentException("Destination path must be provided for the parent folder.");
                }

                // Ensure destination path exists and is a directory
                File parentFolder = new File(destinationPath);
                if (!parentFolder.exists()) {
                    boolean dirsCreated = parentFolder.mkdirs();
                    if (!dirsCreated) {
                        throw new IOException("Failed to create parent folder at: " + destinationPath);
                    }
                } else if (!parentFolder.isDirectory()) {
                    throw new IllegalArgumentException("Destination path must be a directory.");
                }

                // Save the zipped folder to the specified parent folder
                File zipFile = new File(parentFolder, driveFile.getName() + ".zip");

                try (FileOutputStream fos = new FileOutputStream(zipFile)) {
                    fos.write(folderBytes);
                }

                return new GoogleDriveResponseResource(true, response.code());
            } else {
                return new GoogleDriveResponseResource(false, response.code());
            }
        } catch (IOException e) {
            throw new RuntimeException("Error downloading folder", e);
        }
    }

    @Override
    public GoogleDriveResponseResource deleteFile(String fileId, String authToken) {
        HttpUrl.Builder urlBuilder = HttpUrl.parse(URL + "/delete").newBuilder()
                .addQueryParameter("fileId", fileId);

        Request request = createRequestBuilder(urlBuilder.build(), authToken)
                .delete()
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (response.isSuccessful()) {
                return new GoogleDriveResponseResource(true, response.code());
            } else {
                return new GoogleDriveResponseResource(false, response.code());
            }
        } catch (IOException e) {
            throw new RuntimeException("Error deleting file", e);
        }
    }
}
