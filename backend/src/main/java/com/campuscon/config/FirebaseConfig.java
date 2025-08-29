package com.campuscon.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
@Slf4j
public class FirebaseConfig {

    @Value("${firebase.config.path:}")
    private String firebaseConfigPath;

    // Optional: base64-encoded service account json (useful for container env vars)
    @Value("${firebase.credentials.base64:}")
    private String firebaseCredentialsBase64;

    // Optional: raw JSON string for credentials (as a secret mount)
    @Value("${firebase.credentials.json:}")
    private String firebaseCredentialsJson;

    @Bean
    @ConditionalOnProperty(name = "app.notifications.enabled", havingValue = "true", matchIfMissing = true)
    public FirebaseMessaging firebaseMessaging() throws IOException {
        // Only initialize if not already initialized
        if (FirebaseApp.getApps().isEmpty()) {
            try (InputStream credentialsStream = resolveCredentialsStream()) {
                GoogleCredentials credentials = GoogleCredentials.fromStream(credentialsStream);

                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(credentials)
                        .build();

                FirebaseApp.initializeApp(options);
                log.info("FirebaseApp initialized successfully");
            } catch (Exception e) {
                log.error("Failed to initialize FirebaseApp", e);
                throw e;
            }
        } else {
            log.debug("FirebaseApp already initialized, reusing existing instance");
        }

        return FirebaseMessaging.getInstance();
    }

    @Bean(destroyMethod = "shutdown")
    @ConditionalOnProperty(name = "app.notifications.enabled", havingValue = "true", matchIfMissing = true)
    public ExecutorService notificationExecutor(@Value("${app.notifications.async.pool-size:8}") int poolSize) {
        log.info("Initializing notificationExecutor with pool size {}", poolSize);
        return Executors.newFixedThreadPool(Math.max(1, poolSize));
    }

    private InputStream resolveCredentialsStream() throws IOException {
        // Priority: base64 > raw json > path
        if (firebaseCredentialsBase64 != null && !firebaseCredentialsBase64.isBlank()) {
            byte[] decoded = Base64.getDecoder().decode(firebaseCredentialsBase64);
            return new ByteArrayInputStream(decoded);
        }
        if (firebaseCredentialsJson != null && !firebaseCredentialsJson.isBlank()) {
            return new ByteArrayInputStream(firebaseCredentialsJson.getBytes(StandardCharsets.UTF_8));
        }
        if (firebaseConfigPath != null && !firebaseConfigPath.isBlank()) {
            // Try classpath first
            Resource classpath = new ClassPathResource(firebaseConfigPath);
            if (classpath.exists()) {
                return classpath.getInputStream();
            }
            // Then try filesystem
            Resource file = new FileSystemResource(firebaseConfigPath);
            if (file.exists()) {
                return file.getInputStream();
            }
            throw new IOException("Firebase credentials file not found at path: " + firebaseConfigPath);
        }
        throw new IOException("No Firebase credentials provided. Set one of: firebase.credentials.base64, firebase.credentials.json, or firebase.config.path");
    }
}
