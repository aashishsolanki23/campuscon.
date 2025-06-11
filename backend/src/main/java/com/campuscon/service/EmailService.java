package com.campuscon.service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
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
