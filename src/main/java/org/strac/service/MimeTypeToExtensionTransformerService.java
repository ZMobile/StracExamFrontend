package org.strac.service;

public interface MimeTypeToExtensionTransformerService {
    /**
     * Get the file extension for a given MIME type.
     *
     * @param mimeType The MIME type to get the extension for.
     * @return The file extension for the given MIME type.
     */
    String getFileExtensionFromMimeType(String mimeType);
}
