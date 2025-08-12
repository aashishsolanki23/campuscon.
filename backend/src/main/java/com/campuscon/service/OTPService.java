package com.campuscon.service;

import com.campuscon.model.OTP;
import com.campuscon.repository.OTPRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
public class OTPService {
    
    private final OTPRepository otpRepository;
    private final EmailService emailService;
    
    @Value("${otp.expiry.minutes:10}")
    private int otpExpiryMinutes;
    
    @Value("${otp.length:6}")
    private int otpLength;
    
    @Value("${otp.max.attempts.per.hour:5}")
    private int maxAttemptsPerHour;
    
    private final Random random = new Random();
    
    /**
     * Generate and send OTP for email verification
     * @param email the email address
     * @param type the type of OTP
     * @return true if OTP was generated and sent successfully
     */
    public boolean generateAndSendOTP(String email, OTP.OTPType type) {
        try {
            // Check if user has exceeded max attempts
            if (hasExceededMaxAttempts(email, type)) {
                log.warn("User {} has exceeded max OTP attempts for type {}", email, type);
                return false;
            }
            
            // Generate OTP
            String otpCode = generateOTPCode();
            
            // Create OTP entity
            OTP otp = new OTP();
            otp.setEmail(email);
            otp.setOtpCode(otpCode);
            otp.setType(type);
            otp.setExpiryTime(LocalDateTime.now().plusMinutes(otpExpiryMinutes));
            
            // Save OTP
            otpRepository.save(otp);
            
            // Send email
            boolean emailSent = sendOTPEmail(email, otpCode, type);
            
            if (emailSent) {
                log.info("OTP generated and sent successfully for {} (type: {})", email, type);
                return true;
            } else {
                log.error("Failed to send OTP email for {} (type: {})", email, type);
                return false;
            }
            
        } catch (Exception e) {
            log.error("Error generating OTP for {} (type: {})", email, type, e);
            return false;
        }
    }
    
    /**
     * Verify OTP code
     * @param email the email address
     * @param otpCode the OTP code to verify
     * @param type the type of OTP
     * @return true if OTP is valid
     */
    public boolean verifyOTP(String email, String otpCode, OTP.OTPType type) {
        try {
            Optional<OTP> otpOpt = otpRepository.findByEmailAndOtpCodeAndTypeAndUsedFalse(email, otpCode, type);
            
            if (otpOpt.isPresent()) {
                OTP otp = otpOpt.get();
                
                if (otp.isValid()) {
                    // Mark OTP as used
                    otp.setUsed(true);
                    otpRepository.save(otp);
                    
                    // Mark all other OTPs of same type and email as used
                    otpRepository.markAllOTPsAsUsed(email, type);
                    
                    log.info("OTP verified successfully for {} (type: {})", email, type);
                    return true;
                } else {
                    log.warn("OTP expired for {} (type: {})", email, type);
                }
            } else {
                log.warn("Invalid OTP code for {} (type: {})", email, type);
            }
            
            return false;
            
        } catch (Exception e) {
            log.error("Error verifying OTP for {} (type: {})", email, type, e);
            return false;
        }
    }
    
    /**
     * Generate a random OTP code
     * @return the generated OTP code
     */
    private String generateOTPCode() {
        StringBuilder otp = new StringBuilder();
        for (int i = 0; i < otpLength; i++) {
            otp.append(random.nextInt(10));
        }
        return otp.toString();
    }
    
    /**
     * Send OTP email
     * @param email the email address
     * @param otpCode the OTP code
     * @param type the type of OTP
     * @return true if email was sent successfully
     */
    private boolean sendOTPEmail(String email, String otpCode, OTP.OTPType type) {
        try {
            String subject = getOTPEmailSubject(type);
            String body = getOTPEmailBody(otpCode, type);
            
            return emailService.sendEmail(email, subject, body);
            
        } catch (Exception e) {
            log.error("Error sending OTP email to {}", email, e);
            return false;
        }
    }
    
    /**
     * Get OTP email subject based on type
     * @param type the OTP type
     * @return the email subject
     */
    private String getOTPEmailSubject(OTP.OTPType type) {
        switch (type) {
            case EMAIL_VERIFICATION:
                return "Verify Your Email - CampusCon";
            case PASSWORD_RESET:
                return "Reset Your Password - CampusCon";
            case PHONE_VERIFICATION:
                return "Verify Your Phone - CampusCon";
            default:
                return "Your OTP Code - CampusCon";
        }
    }
    
    /**
     * Get OTP email body
     * @param otpCode the OTP code
     * @param type the OTP type
     * @return the email body
     */
    private String getOTPEmailBody(String otpCode, OTP.OTPType type) {
        String action = type == OTP.OTPType.PASSWORD_RESET ? "reset your password" : "verify your account";
        
        return String.format("""
            <html>
            <body>
                <h2>CampusCon Verification</h2>
                <p>Your OTP code to %s is: <strong>%s</strong></p>
                <p>This code will expire in %d minutes.</p>
                <p>If you didn't request this code, please ignore this email.</p>
                <br>
                <p>Best regards,<br>The CampusCon Team</p>
            </body>
            </html>
            """, action, otpCode, otpExpiryMinutes);
    }
    
    /**
     * Check if user has exceeded max attempts
     * @param email the email address
     * @param type the OTP type
     * @return true if max attempts exceeded
     */
    private boolean hasExceededMaxAttempts(String email, OTP.OTPType type) {
        LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
        long recentAttempts = otpRepository.countRecentOTPsByEmailAndType(email, type, oneHourAgo);
        return recentAttempts >= maxAttemptsPerHour;
    }
    
    /**
     * Clean up expired OTPs (scheduled task)
     */
    @Scheduled(fixedRate = 300000) // Run every 5 minutes
    public void cleanupExpiredOTPs() {
        try {
            LocalDateTime now = LocalDateTime.now();
            otpRepository.deleteExpiredOTPs(now);
            log.debug("Cleaned up expired OTPs");
        } catch (Exception e) {
            log.error("Error cleaning up expired OTPs", e);
        }
    }
}
