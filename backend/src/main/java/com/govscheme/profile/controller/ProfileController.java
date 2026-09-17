package com.govscheme.profile.controller;

import com.govscheme.common.dto.ApiResponse;
import com.govscheme.profile.dto.ProfileRequest;
import com.govscheme.profile.dto.ProfileResponse;
import com.govscheme.profile.service.ProfileService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Citizen profile. The user is always resolved from the security principal,
 * so endpoints carry no user id and cross-user access is impossible by design.
 * <p>
 * {@code PUT} merges: {@code null} fields leave stored values untouched, while
 * a present {@code addresses} list replaces the stored address set.
 */
@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<ProfileResponse>> getProfile(
            @AuthenticationPrincipal UserDetails principal) {
        ProfileResponse response = profileService.getProfile(principal.getUsername());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping
    public ResponseEntity<ApiResponse<ProfileResponse>> updateProfile(
            @AuthenticationPrincipal UserDetails principal,
            @Valid @RequestBody ProfileRequest request) {
        ProfileResponse response = profileService.updateProfile(principal.getUsername(), request);
        return ResponseEntity.ok(ApiResponse.success(response, "Profile saved"));
    }
}
