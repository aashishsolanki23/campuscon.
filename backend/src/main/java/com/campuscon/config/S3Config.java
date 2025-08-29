package com.campuscon.config;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class S3Config {
    
    @Value("${aws.access-key:}")
    private String accessKey;
    
    @Value("${aws.secret-key:}")
    private String secretKey;
    
    @Value("${aws.region:us-east-1}")
    private String region;
    
    @Value("${aws.s3.bucket}")
    private String bucketName;
    
    @Bean
    @ConditionalOnProperty(name = "app.aws.s3.enabled", havingValue = "true", matchIfMissing = true)
    public AmazonS3 amazonS3Client() {
        AmazonS3ClientBuilder builder = AmazonS3ClientBuilder.standard()
                .withRegion(Regions.fromName(region));
        
        // Use default credentials provider chain for production-ready deployment
        // This supports IAM roles, environment variables, credential files, etc.
        if (accessKey != null && !accessKey.isBlank() && 
            secretKey != null && !secretKey.isBlank()) {
            // Only use explicit credentials if provided (for development)
            log.warn("Using explicit AWS credentials. For production, prefer IAM roles or default credential chain.");
            BasicAWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);
            builder.withCredentials(new AWSStaticCredentialsProvider(credentials));
        } else {
            // Use AWS default credentials provider chain (preferred for production)
            log.info("Using AWS default credentials provider chain (IAM roles, environment variables, etc.)");
            builder.withCredentials(DefaultAWSCredentialsProviderChain.getInstance());
        }
        
        return builder.build();
    }
    
    public String getBucketName() {
        return bucketName;
    }
}
