package com.fundoonotes.fundoo_notes.service;

import com.fundoonotes.fundoo_notes.dto.LoginDTO;
import com.fundoonotes.fundoo_notes.dto.ResetPasswordOtpDTO;
import com.fundoonotes.fundoo_notes.dto.UserDTO;
import com.fundoonotes.fundoo_notes.dto.VerifyOtpDTO;

public interface UserService {

    String register(UserDTO dto);

    // OLD token based - keep for backward compatibility
    String verifyEmail(String token);

    String login(LoginDTO dto);

    String forgotPassword(String email);

    String resetPassword(String token, String newPassword);

    // NEW OTP METHODS
    String verifyOtp(VerifyOtpDTO dto);

    String resendOtp(String email);

    String forgotPasswordOtp(String email);

    String resetPasswordWithOtp(ResetPasswordOtpDTO dto);

    // LOGOUT — blacklists the current JWT in Redis
    String logout(String token);
}