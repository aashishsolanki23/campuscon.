package com.campuscon.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;

@Service
@Slf4j
public class QRCodeService {

    @Value("${app.qr.storage.path:uploads/qr-codes}")
    private String qrStoragePath;

    @Value("${app.qr.base64.enabled:true}")
    private boolean useBase64;

    /**
     * Generate QR code and return either file path or Base64 string
     */
    public String generateQRCode(String content, String fileName) {
        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, 300, 300);

            if (useBase64) {
                return generateBase64QRCode(bitMatrix);
            } else {
                return saveQRCodeToFile(bitMatrix, fileName);
            }
        } catch (WriterException | IOException e) {
            log.error("Error generating QR code for content: {}", content, e);
            throw new RuntimeException("Failed to generate QR code", e);
        }
    }

    private String generateBase64QRCode(BitMatrix bitMatrix) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);
        byte[] qrCodeBytes = outputStream.toByteArray();
        return "data:image/png;base64," + Base64.getEncoder().encodeToString(qrCodeBytes);
    }

    private String saveQRCodeToFile(BitMatrix bitMatrix, String fileName) throws IOException {
        // Create directory if it doesn't exist
        Path directory = Paths.get(qrStoragePath);
        if (!Files.exists(directory)) {
            Files.createDirectories(directory);
        }

        // Save QR code as PNG file
        Path filePath = directory.resolve(fileName + ".png");
        MatrixToImageWriter.writeToPath(bitMatrix, "PNG", filePath);
        
        log.info("QR code saved to: {}", filePath.toString());
        return filePath.toString();
    }

    /**
     * Generate ticket code in format D123-U456-ABC789
     */
    public String generateTicketCode(Long deedId, Long userId) {
        String randomSuffix = generateRandomString(6);
        return String.format("D%d-U%d-%s", deedId, userId, randomSuffix);
    }

    private String generateRandomString(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < length; i++) {
            result.append(chars.charAt((int) (Math.random() * chars.length())));
        }
        return result.toString();
    }
}
