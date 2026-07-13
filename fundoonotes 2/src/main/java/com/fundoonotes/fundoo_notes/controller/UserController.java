package com.fundoonotes.fundoo_notes.controller;

import com.fundoonotes.fundoo_notes.dto.*;
import com.fundoonotes.fundoo_notes.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    // REGISTER
    @PostMapping("/register")
    public ResponseEntity<String> register(@Valid @RequestBody UserDTO dto) {
        String message = userService.register(dto);
        return new ResponseEntity<>(message, HttpStatus.CREATED);
    }

    // VERIFY EMAIL WITH OTP (NEW)
    @PostMapping("/verify-otp")
    public ResponseEntity<ApiResponse> verifyOtp(@Valid @RequestBody VerifyOtpDTO dto) {
        String message = userService.verifyOtp(dto);
        return ResponseEntity.ok(new ApiResponse(200, message));
    }

    // RESEND OTP (NEW)
    @PostMapping("/resend-otp")
    public ResponseEntity<ApiResponse> resendOtp(@RequestParam String email) {
        String message = userService.resendOtp(email);
        return ResponseEntity.ok(new ApiResponse(200, message));
    }

    // VERIFY EMAIL WITH TOKEN (OLD - keep for backward compatibility)
    @GetMapping("/verify")
    public ResponseEntity<ApiResponse> verifyEmail(@RequestParam String token) {
        String message = userService.verifyEmail(token);
        return ResponseEntity.ok(new ApiResponse(200, message));
    }

    // LOGIN
    @PostMapping("/login")
    public ResponseEntity<ApiResponse> login(@Valid @RequestBody LoginDTO dto) {
        String token = userService.login(dto);
        return ResponseEntity.ok()
                .header("Authorization", "Bearer " + token)
                .body(new ApiResponse(200, "Login successful"));
    }

    // LOGOUT — blacklist the current token
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse> logout(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.startsWith("Bearer ")
                ? authHeader.substring(7)
                : authHeader;
        String message = userService.logout(token);
        return ResponseEntity.ok(new ApiResponse(200, message));
    }

    // FORGOT PASSWORD WITH OTP (NEW)
    @PostMapping("/forgot-password-otp")
    public ResponseEntity<ApiResponse> forgotPasswordOtp(@RequestParam String email) {
        String message = userService.forgotPasswordOtp(email);
        return ResponseEntity.ok(new ApiResponse(200, message));
    }

    // RESET PASSWORD WITH OTP (NEW)
    @PostMapping("/reset-password-otp")
    public ResponseEntity<ApiResponse> resetPasswordWithOtp(@Valid @RequestBody ResetPasswordOtpDTO dto) {
        String message = userService.resetPasswordWithOtp(dto);
        return ResponseEntity.ok(new ApiResponse(200, message));
    }

    // FORGOT PASSWORD WITH LINK (OLD - keep)
    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse> forgotPassword(@RequestParam String email) {
        String message = userService.forgotPassword(email);
        return ResponseEntity.ok(new ApiResponse(200, message));
    }

    // RESET PASSWORD WITH TOKEN (OLD - keep)
    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse> resetPassword(@RequestParam String token, @RequestParam String newPassword) {
        String message = userService.resetPassword(
                token, newPassword);
        return ResponseEntity.ok(new ApiResponse(200, message));

    }
}