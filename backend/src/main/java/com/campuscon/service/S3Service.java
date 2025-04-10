package com.campuscon.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

@Service
@RequiredArgsConstructor
@Slf4j
public class S3Service {

    private final AmazonS3 amazonS3;
    
    @Value("${aws.s3.region}")
    private String region;

    /**
     * Upload a file to S3
     * 
     * @param file The file to upload
     * @param key The key (path) to store the file under
     * @param bucketName The S3 bucket name
     * @return The URL of the uploaded file
     */
    public String uploadFile(MultipartFile file, String key, String bucketName) throws IOException {
        // Check if file is empty
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Empty file cannot be uploaded");
        }

        // Set the file metadata
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(file.getContentType());
        metadata.setContentLength(file.getSize());

        // Upload the file to S3
        try (InputStream inputStream = file.getInputStream()) {
            amazonS3.putObject(new PutObjectRequest(
                    bucketName, 
                    key, 
                    inputStream, 
                    metadata)
                    .withCannedAcl(CannedAccessControlList.PublicRead));
        } catch (IOException e) {
            log.error("Error uploading file to S3", e);
            throw e;
        }

        // Return the public URL
        return String.format("https://%s.s3.%s.amazonaws.com/%s", bucketName, region, key);
    }

    /**
     * Delete a file from S3
     * 
     * @param key The key (path) of the file to delete
     * @param bucketName The S3 bucket name
     */
    public void deleteFile(String key, String bucketName) {
        try {
            amazonS3.deleteObject(new DeleteObjectRequest(bucketName, key));
            log.info("Successfully deleted file from S3: {}", key);
        } catch (Exception e) {
            log.error("Error deleting file from S3", e);
            throw e;
        }
    }

    /**
     * Generate a presigned URL for temporary access to a file
     * 
     * @param key The key (path) of the file
     * @param bucketName The S3 bucket name
     * @param expirationInMs The expiration time in milliseconds
     * @return The presigned URL
     */
    public String generatePresignedUrl(String key, String bucketName, long expirationInMs) {
        java.util.Date expiration = new java.util.Date();
        expiration.setTime(expiration.getTime() + expirationInMs);
        
        return amazonS3.generatePresignedUrl(bucketName, key, expiration).toString();
    }
}
