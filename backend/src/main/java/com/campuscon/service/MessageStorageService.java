package com.campuscon.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.UUID;

/**
 * Service for handling storage of message attachments like images.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MessageStorageService {

    @Value("${app.message-attachments.dir:./message-uploads}")
    private String uploadDir;
    
    @Value("${app.message-attachments.url-prefix:/api/attachments}")
    private String urlPrefix;
    
    /**
     * Store a base64 encoded image and return the URL to access it.
     *
     * @param base64Image Base64 encoded image data (with MIME type prefix)
     * @param prefix Prefix to use in the filename
     * @return URL to access the stored image
     */
    public String storeBase64Image(String base64Image, String prefix) {
        try {
            // Create upload directory if it doesn't exist
            Files.createDirectories(Paths.get(uploadDir));
            
            // Extract MIME type and decode image data
            String[] parts = base64Image.split(",");
            String imageType = parts[0].split(";")[0].split(":")[1];
            String extension = getExtensionFromMimeType(imageType);
            byte[] decodedImage = Base64.getDecoder().decode(parts[1]);
            
            // Generate a unique filename
            String filename = prefix + "_" + UUID.randomUUID() + "." + extension;
            File outputFile = new File(uploadDir, filename);
            
            // Write the image to disk
            try (FileOutputStream outputStream = new FileOutputStream(outputFile)) {
                outputStream.write(decodedImage);
            }
            
            // Return the relative URL to access the image
            return urlPrefix + "/" + filename;
        } catch (Exception e) {
            log.error("Error saving base64 image", e);
            return null;
        }
    }
    
    /**
     * Extract file extension from MIME type.
     *
     * @param mimeType The MIME type (e.g., "image/jpeg")
     * @return The file extension (e.g., "jpg")
     */
    private String getExtensionFromMimeType(String mimeType) {
        switch (mimeType.toLowerCase()) {
            case "image/jpeg":
            case "image/jpg":
                return "jpg";
            case "image/png":
                return "png";
            case "image/gif":
                return "gif";
            case "image/webp":
                return "webp";
            default:
                return "jpg"; // Default to jpg
        }
    }
    
    /**
     * Delete a stored file by its URL.
     *
     * @param fileUrl URL of the file to delete
     * @return true if deletion was successful, false otherwise
     */
    public boolean deleteFile(String fileUrl) {
        if (fileUrl == null || !fileUrl.startsWith(urlPrefix)) {
            return false;
        }
        
        try {
            // Extract the filename from the URL
            String filename = fileUrl.substring(urlPrefix.length() + 1);
            Path filePath = Paths.get(uploadDir, filename);
            
            // Delete the file if it exists
            if (Files.exists(filePath)) {
                Files.delete(filePath);
                return true;
            }
            
            return false;
        } catch (Exception e) {
            log.error("Error deleting file: " + fileUrl, e);
            return false;
        }
    }
}
