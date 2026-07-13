package com.fundoonotes.fundoo_notes.service.impl;

import com.fundoonotes.fundoo_notes.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpl implements EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Override
    public void sendVerificationEmail(String toEmail, String token) {
        String link = "http://localhost:8080" +
                "/api/users/verify?token=" + token;
        sendEmail(toEmail,
                "Verify Your Fundoo Notes Account",
                "Hello,\n\nClick to verify your account:\n\n"
                        + link + "\n\nThis link expires in 24 hours.\n\n"
                        + "Regards,\nFundoo Notes Team");
    }

    @Override
    public void sendPasswordResetEmail(String toEmail, String token) {
        String link = "http://localhost:8080" +
                "/api/users/reset-password?token=" + token;
        sendEmail(toEmail,
                "Reset Your Fundoo Notes Password",
                "Hello,\n\nClick to reset your password:\n\n"
                        + link + "\n\nThis link expires in 24 hours.\n\n"
                        + "Regards,\nFundoo Notes Team");
    }

    @Override
    public void sendReminderEmail(String toEmail, String noteTitle) {
        sendEmail(toEmail,
                "Reminder: " + noteTitle,
                "Hello,\n\nThis is a reminder for your note:\n\n\""
                        + noteTitle + "\"\n\nPlease check your Fundoo Notes.\n\n"
                        + "Regards,\nFundoo Notes Team");
    }

    @Override
    public void sendOtpEmail(String toEmail, String otp) {
        sendEmail(toEmail,
                "Verify Your Fundoo Notes Account - OTP",
                "Hello,\n\n" +
                        "Your OTP for account verification is:\n\n" +
                        "🔐 " + otp + "\n\n" +
                        "This OTP is valid for 10 minutes.\n\n" +
                        "If you did not register, please ignore this email.\n\n" +
                        "Regards,\nFundoo Notes Team"
        );
    }

    @Override
    public void sendPasswordResetOtpEmail(String toEmail, String otp) {
        sendEmail(toEmail,
                "Reset Your Fundoo Notes Password - OTP",
                "Hello,\n\n" +
                        "Your OTP for password reset is:\n\n" +
                        "🔐 " + otp + "\n\n" +
                        "This OTP is valid for 10 minutes.\n\n" +
                        "If you did not request this, please ignore this email.\n\n" +
                        "Regards,\nFundoo Notes Team"
        );
    }

    private void sendEmail(String to,
                           String subject,
                           String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);
        mailSender.send(message);
    }
}