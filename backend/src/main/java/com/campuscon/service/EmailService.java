package com.campuscon.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {
    private final JavaMailSender mailSender;

    public void sendOTPEmail(String toEmail, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("CampusCon - Email Verification OTP");
        message.setText("Your OTP for email verification is: " + otp + "\n\n" +
                "This OTP will expire in 10 minutes.\n\n" +
                "Welcome to CampusCon - Your Academic Networking Platform!");
        
        mailSender.send(message);
    }
    
    /**
     * Send HTML email with subject and body
     * @param toEmail recipient email
     * @param subject email subject
     * @param htmlBody HTML email body
     * @return true if email sent successfully
     */
    public boolean sendEmail(String toEmail, String subject, String htmlBody) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(htmlBody, true); // true indicates HTML content
            
            mailSender.send(message);
            log.info("HTML email sent successfully to: {}", toEmail);
            return true;
            
        } catch (MessagingException e) {
            log.error("Failed to send HTML email to: {}", toEmail, e);
            return false;
        }
    }
    
    /**
     * Send simple text email
     * @param toEmail recipient email
     * @param subject email subject
     * @param textBody plain text email body
     * @return true if email sent successfully
     */
    public boolean sendTextEmail(String toEmail, String subject, String textBody) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setSubject(subject);
            message.setText(textBody);
            
            mailSender.send(message);
            log.info("Text email sent successfully to: {}", toEmail);
            return true;
            
        } catch (Exception e) {
            log.error("Failed to send text email to: {}", toEmail, e);
            return false;
        }
    }

    // public void sendSocietyVerificationEmail(String toEmail, String societyName, String verificationLink) {
    //     SimpleMailMessage message = new SimpleMailMessage();
    //     message.setTo(toEmail);
    //     message.setSubject("CampusCon - Society Verification Request");
    //     message.setText("Dear Society President,\n\n" +
    //             "A request has been made to register " + societyName + " on CampusCon.\n\n" +
    //             "Please click the following link to verify and approve the society registration:\n" +
    //             verificationLink + "\n\n" +
    //             "If you did not request this registration, please ignore this email.\n\n" +
    //             "Best regards,\nCampusCon Team");
        
    //     mailSender.send(message);
    // }
}
