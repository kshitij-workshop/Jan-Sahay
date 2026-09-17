package com.govscheme.matching.controller;

import com.govscheme.auth.entity.UserRepository;
import com.govscheme.common.dto.ApiResponse;
import com.govscheme.common.exception.ResourceNotFoundException;
import com.govscheme.matching.dto.MatchResponse;
import com.govscheme.matching.entity.UserSchemeMatch;
import com.govscheme.matching.entity.UserSchemeMatchRepository;
import com.govscheme.scheme.entity.Scheme;
import com.govscheme.scheme.entity.SchemeRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * The authenticated user's own stored matches. Like profiles, these endpoints
 * carry no user id: ownership comes from the security principal.
 */
@RestController
@RequestMapping("/api/matches")
public class MatchesController {

    private final UserRepository userRepository;
    private final UserSchemeMatchRepository matchRepository;
    private final SchemeRepository schemeRepository;

    public MatchesController(UserRepository userRepository,
                             UserSchemeMatchRepository matchRepository,
                             SchemeRepository schemeRepository) {
        this.userRepository = userRepository;
        this.matchRepository = matchRepository;
        this.schemeRepository = schemeRepository;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<MatchResponse>>> list(
            @AuthenticationPrincipal UserDetails principal,
            @RequestParam(required = false) UserSchemeMatch.Status status) {
        String userId = userIdFor(principal.getUsername());
        List<UserSchemeMatch> matches = status == null
            ? matchRepository.findByUserId(userId)
            : matchRepository.findByUserIdAndStatus(userId, status);
        return ResponseEntity.ok(ApiResponse.success(matches.stream().map(this::toResponse).toList()));
    }

    @GetMapping("/{schemeId}")
    public ResponseEntity<ApiResponse<MatchResponse>> one(
            @AuthenticationPrincipal UserDetails principal,
            @PathVariable String schemeId) {
        String userId = userIdFor(principal.getUsername());
        UserSchemeMatch match = matchRepository.findByUserIdAndSchemeId(userId, schemeId)
            .orElseThrow(() -> new ResourceNotFoundException("Match", "schemeId", schemeId));
        return ResponseEntity.ok(ApiResponse.success(toResponse(match)));
    }

    private String userIdFor(String username) {
        return userRepository.findByEmailOrPhone(username, username)
            .orElseThrow(() -> new ResourceNotFoundException("User", "username", username))
            .getId();
    }

    private MatchResponse toResponse(UserSchemeMatch match) {
        Scheme scheme = schemeRepository.findById(match.getSchemeId()).orElse(null);
        return MatchResponse.of(match,
            scheme == null ? null : scheme.getSchemeName(),
            scheme == null ? null : scheme.getSlug());
    }
}
