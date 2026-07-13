package com.fundoonotes.fundoo_notes.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(nullable = false, unique = true)
    private String email;

    // Nullable because OAuth2 (Google) users don't have a local password
    private String password;

    @Column(nullable = false)
    private boolean isVerified = false;

    // OTP FIELDS
    private String otp;

    private LocalDateTime otpExpiry;

    // AUTH PROVIDER: "LOCAL" (email+password) or "GOOGLE" (OAuth2)
    @Column(nullable = false)
    private String provider = "LOCAL";

    // Google account's unique subject id (null for LOCAL users)
    private String providerId;

    @CreationTimestamp
    private LocalDateTime createdAt;
}