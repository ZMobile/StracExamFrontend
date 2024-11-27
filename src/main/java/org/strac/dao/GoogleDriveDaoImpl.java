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
    private static final String BASE_URL = "http://localhost:8080/api/drive";
    private static final MediaType JSON_MEDIA_TYPE = MediaType.parse("application/json");
    private final OkHttpClient httpClient;
    private final Gson gson;

    public GoogleDriveDaoImpl(Gson gson) {
        this.httpClient = new OkHttpClient();
        this.gson = gson;
    }

    private Request.Builder createRequestBuilder(HttpUrl url, String authToken) {
        Request.Builder builder = new Request.Builder().url(url);

        // Load the JWT token and add it to the Authorization header
        if (authToken != null) {
            builder.addHeader("Authorization", "Bearer " + authToken);
        }

        return builder;
    }

    @Override
    public GoogleDriveFileQueryResponseResource listFiles(String parentId, String authToken) {
        HttpUrl.Builder urlBuilder = HttpUrl.parse(BASE_URL + "/files").newBuilder();
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
    public GoogleDriveResponseResource uploadFile(String parentId, String filePath, String authToken) {
        File file = new File(filePath);
        if (!file.exists()) {
            throw new IllegalArgumentException("File does not exist: " + filePath);
        }

        HttpUrl.Builder urlBuilder = HttpUrl.parse(BASE_URL + "/upload").newBuilder();
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
    public GoogleDriveResponseResource downloadFile(String fileId, String destinationPath, String authToken) {
        HttpUrl.Builder urlBuilder = Objects.requireNonNull(HttpUrl.parse(BASE_URL + "/download/file")).newBuilder()
                .addQueryParameter("fileId", fileId);

        Request request = createRequestBuilder(urlBuilder.build(), authToken)
                .get()
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                try (FileOutputStream fos = new FileOutputStream(destinationPath)) {
                    fos.write(response.body().bytes());
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
    public GoogleDriveResponseResource downloadFolder(String folderId, String destinationPath, String authToken) {
        HttpUrl.Builder urlBuilder = Objects.requireNonNull(HttpUrl.parse(BASE_URL + "/download/folder")).newBuilder()
                .addQueryParameter("folderId", folderId);

        Request request = createRequestBuilder(urlBuilder.build(), authToken)
                .get()
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                try (FileOutputStream fos = new FileOutputStream(destinationPath)) {
                    fos.write(response.body().bytes());
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
        HttpUrl.Builder urlBuilder = HttpUrl.parse(BASE_URL + "/delete").newBuilder()
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
