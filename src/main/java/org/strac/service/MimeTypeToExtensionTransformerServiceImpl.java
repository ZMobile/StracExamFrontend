package org.strac.service;

public class MimeTypeToExtensionTransformerServiceImpl implements MimeTypeToExtensionTransformerService {
    public String getFileExtensionFromMimeType(String mimeType) {
        switch (mimeType) {
            // Google-specific MIME types
            case "application/vnd.google-apps.document":
                return ".docx"; // Google Docs exported as Word document
            case "application/vnd.google-apps.spreadsheet":
                return ".xlsx"; // Google Sheets exported as Excel document
            case "application/vnd.google-apps.presentation":
                return ".pptx"; // Google Slides exported as PowerPoint
            case "application/vnd.google-apps.folder":
                return ""; // Folders don't have extensions
            case "application/vnd.google-apps.shortcut":
                return ""; // Shortcuts don't have extensions

            // Video MIME types
            case "video/mp4":
                return ".mp4";
            case "video/quicktime":
                return ".mov";

            // Audio MIME types
            case "audio/mpeg":
                return ".mp3";
            case "audio/x-wav":
                return ".wav";

            // Common document MIME types
            case "application/pdf":
                return ".pdf";
            case "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet":
                return ".xlsx";
            case "application/vnd.openxmlformats-officedocument.wordprocessingml.document":
                return ".docx";
            case "application/msword":
                return ".doc";
            case "application/vnd.openxmlformats-officedocument.presentationml.presentation":
                return ".pptx";
            case "text/plain":
                return ".txt";

            // Image MIME types
            case "image/jpeg":
                return ".jpg";
            case "image/png":
                return ".png";

            // Compressed file MIME types
            case "application/zip":
                return ".zip";
            case "application/x-7z-compressed":
                return ".7z";

            // Default case for unknown types
            default:
                System.out.println("Unknown MIME type: " + mimeType + ". Defaulting to .bin.");
                return ".bin"; // Default to binary file
        }
    }

}
