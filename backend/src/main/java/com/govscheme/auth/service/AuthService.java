package com.govscheme.auth.service;

import com.govscheme.auth.dto.AuthResponse;
import com.govscheme.auth.dto.LoginRequest;
import com.govscheme.auth.dto.RegisterRequest;
import com.govscheme.auth.entity.User;
import com.govscheme.auth.entity.UserRepository;
import com.govscheme.common.exception.ResourceNotFoundException;
import com.govscheme.common.exception.ValidationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService, AuthenticationManager authenticationManager, EmailService emailService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.emailService = emailService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ValidationException("Email already registered", Map.of("email", "Email already exists"));
        }
        if (userRepository.existsByPhone(request.getPhone())) {
            throw new ValidationException("Phone already registered", Map.of("phone", "Phone already exists"));
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setFullName(request.getFullName());
        user.setRole(User.Role.USER);
        user.setIsActive(true);
        user.setEmailVerified(false);
        user.setPhoneVerified(false);

        // Generate email verification token
        String verificationToken = UUID.randomUUID().toString();
        user.setEmailVerificationToken(verificationToken);
        user.setEmailVerificationTokenExpiry(Instant.now().plusSeconds(24 * 60 * 60)); // 24 hours

        userRepository.save(user);

        // Send verification email
        emailService.sendEmailVerification(user.getEmail(), user.getFullName(), verificationToken);

        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        return AuthResponse.of(accessToken, refreshToken, jwtService.getJwtProperties().getAccessTokenExpiryMs() / 1000, user);
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.getIdentifier(), request.getPassword())
        );

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        User user = userRepository.findByEmailOrPhone(request.getIdentifier(), request.getIdentifier())
            .orElseThrow(() -> new ResourceNotFoundException("User", "identifier", request.getIdentifier()));

        user.setLastLoginAt(java.time.Instant.now());
        userRepository.save(user);

        String accessToken = jwtService.generateAccessToken(userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails);

        return AuthResponse.of(accessToken, refreshToken, jwtService.getJwtProperties().getAccessTokenExpiryMs() / 1000, user);
    }

    public AuthResponse refreshToken(String refreshToken) {
        if (!jwtService.isRefreshToken(refreshToken)
                || !jwtService.isTokenValid(refreshToken, loadUserByUsernameFromToken(refreshToken))) {
            throw new com.govscheme.common.exception.ValidationException("Invalid refresh token", Map.of());
        }

        UserDetails userDetails = loadUserByUsernameFromToken(refreshToken);
        User user = userRepository.findByEmailOrPhone(userDetails.getUsername(), userDetails.getUsername())
            .orElseThrow(() -> new ResourceNotFoundException("User", "username", userDetails.getUsername()));

        String newAccessToken = jwtService.generateAccessToken(userDetails);
        String newRefreshToken = jwtService.generateRefreshToken(userDetails);

        return AuthResponse.of(newAccessToken, newRefreshToken, jwtService.getJwtProperties().getAccessTokenExpiryMs() / 1000, user);
    }

    private UserDetails loadUserByUsernameFromToken(String token) {
        String username = jwtService.extractUsername(token);
        return userRepository.findByEmailOrPhone(username, username)
            .orElseThrow(() -> new com.govscheme.common.exception.ResourceNotFoundException("User", "username", username));
    }

    public User getCurrentUser(String accessToken) {
        String username = jwtService.extractUsername(accessToken);
        return getCurrentUserByUsername(username);
    }

    public User getCurrentUserByUsername(String username) {
        return userRepository.findByEmailOrPhone(username, username)
            .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));
    }

    @Transactional
    public void verifyEmail(String token) {
        User user = userRepository.findByEmailVerificationToken(token)
            .orElseThrow(() -> new ValidationException("Invalid verification token", Map.of("token", "Invalid or expired token")));

        if (user.getEmailVerificationTokenExpiry() == null
                || user.getEmailVerificationTokenExpiry().isBefore(Instant.now())) {
            throw new ValidationException("Verification token expired", Map.of("token", "Token has expired"));
        }

        if (Boolean.TRUE.equals(user.getEmailVerified())) {
            throw new ValidationException("Email already verified", Map.of("email", "Email is already verified"));
        }

        user.setEmailVerified(true);
        user.setEmailVerificationToken(null);
        user.setEmailVerificationTokenExpiry(null);
        userRepository.save(user);
    }

    @Transactional
    public void resendVerificationEmail(String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new ValidationException("User not found", Map.of("email", "User not found")));

        if (Boolean.TRUE.equals(user.getEmailVerified())) {
            throw new ValidationException("Email already verified", Map.of("email", "Email is already verified"));
        }

        String verificationToken = UUID.randomUUID().toString();
        user.setEmailVerificationToken(verificationToken);
        user.setEmailVerificationTokenExpiry(Instant.now().plusSeconds(24 * 60 * 60));
        userRepository.save(user);

        emailService.sendEmailVerification(user.getEmail(), user.getFullName(), verificationToken);
    }
}