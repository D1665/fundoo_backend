package com.fundoonotes.fundoo_notes.service;

public interface EmailService {

    void sendVerificationEmail(String toEmail, String token);

    void sendPasswordResetEmail(String toEmail, String token);

    void sendReminderEmail(String toEmail, String noteTitle);

    // NEW OTP METHODS
    void sendOtpEmail(String toEmail, String otp);

    void sendPasswordResetOtpEmail(String toEmail, String otp);
}