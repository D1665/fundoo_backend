package com.fundoonotes.fundoo_notes.service.impl;

import com.fundoonotes.fundoo_notes.dto.*;
import com.fundoonotes.fundoo_notes.model.User;
import com.fundoonotes.fundoo_notes.repository.UserRepository;
import com.fundoonotes.fundoo_notes.security.JwtUtil;
import com.fundoonotes.fundoo_notes.service.EmailService;
import com.fundoonotes.fundoo_notes.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private EmailService emailService;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    // GENERATE 6 DIGIT OTP
    private String generateOtp() {
        Random random = new Random();
        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);
    }

    @Override
    public String register(UserDTO dto) {
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("Email already registered");
        }

        String otp = generateOtp();

        User user = new User();
        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setVerified(false);
        user.setOtp(otp);
        user.setOtpExpiry(LocalDateTime.now().plusMinutes(10));
        userRepository.save(user);

        // Send OTP email instead of link
        emailService.sendOtpEmail(dto.getEmail(), otp);

        return "Registration successful. " +
                "Please check your email for OTP.";
    }

    // NEW — Verify with OTP
    @Override
    public String verifyOtp(VerifyOtpDTO dto) {
        User user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        if (user.isVerified()) {
            return "Email already verified. Please login.";
        }

        if (user.getOtp() == null ||
                !user.getOtp().equals(dto.getOtp())) {
            throw new RuntimeException("Invalid OTP");
        }

        if (user.getOtpExpiry() == null ||
                LocalDateTime.now().isAfter(user.getOtpExpiry())) {
            throw new RuntimeException(
                    "OTP expired. Please request a new one.");
        }

        user.setVerified(true);
        user.setOtp(null);
        user.setOtpExpiry(null);
        userRepository.save(user);

        return "Email verified successfully. You can now login.";
    }

    // RESEND OTP
    @Override
    public String resendOtp(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        if (user.isVerified()) {
            return "Email already verified. Please login.";
        }

        String otp = generateOtp();
        user.setOtp(otp);
        user.setOtpExpiry(LocalDateTime.now().plusMinutes(10));
        userRepository.save(user);

        emailService.sendOtpEmail(email, otp);
        return "OTP resent successfully. Please check your email.";
    }

    // OLD token based verify — keep for backward compatibility
    @Override
    public String verifyEmail(String token) {
        String email = jwtUtil.extractEmail(token);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));
        if (user.isVerified()) {
            return "Email already verified. Please login.";
        }
        user.setVerified(true);
        userRepository.save(user);
        return "Email verified successfully. You can now login.";
    }

    @Override
    public String login(LoginDTO dto) {
        User user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid email"));

        if (!"LOCAL".equals(user.getProvider()) || user.getPassword() == null) {
            throw new RuntimeException(
                    "This account uses Google Sign-In. Please login with Google.");
        }

        if (!passwordEncoder.matches(
                dto.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid password");
        }

        if (!user.isVerified()) {
            throw new RuntimeException(
                    "Please verify your email first.");
        }

        return jwtUtil.generateToken(dto.getEmail());
    }

    // FORGOT PASSWORD — Send OTP
    @Override
    public String forgotPasswordOtp(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException(
                                "No account found with this email"));

        String otp = generateOtp();
        user.setOtp(otp);
        user.setOtpExpiry(LocalDateTime.now().plusMinutes(10));
        userRepository.save(user);

        emailService.sendPasswordResetOtpEmail(email, otp);
        return "OTP sent to your email for password reset.";
    }

    // RESET PASSWORD WITH OTP
    @Override
    public String resetPasswordWithOtp(ResetPasswordOtpDTO dto) {
        User user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        if (user.getOtp() == null ||
                !user.getOtp().equals(dto.getOtp())) {
            throw new RuntimeException("Invalid OTP");
        }

        if (user.getOtpExpiry() == null ||
                LocalDateTime.now().isAfter(user.getOtpExpiry())) {
            throw new RuntimeException(
                    "OTP expired. Please request a new one.");
        }

        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        user.setOtp(null);
        user.setOtpExpiry(null);
        userRepository.save(user);

        return "Password reset successful. You can now login.";
    }

    // OLD token based forgot password — keep for backward compatibility
    @Override
    public String forgotPassword(String email) {
        userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException(
                                "No account found with this email"));
        String token = jwtUtil.generateToken(email);
        emailService.sendPasswordResetEmail(email, token);
        return "Password reset link sent to your email.";
    }

    // OLD token based reset password — keep for backward compatibility
    @Override
    public String resetPassword(String token, String newPassword) {
        String email = jwtUtil.extractEmail(token);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("Invalid or expired token"));
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        return "Password reset successful. You can now login.";
    }

    // LOGOUT — remove the cached token and mark it blacklisted in Redis
    // so JwtFilter rejects it even though it hasn't expired yet
    @Override
    public String logout(String token) {
        redisTemplate.delete("TOKEN:" + token);
        redisTemplate.opsForValue().set(
                "BLACKLIST:" + token, "true", 24, TimeUnit.HOURS);
        return "Logged out successfully.";
    }
}