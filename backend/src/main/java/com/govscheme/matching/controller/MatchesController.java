package com.govscheme.matching.controller;

import com.govscheme.auth.entity.UserRepository;
import com.govscheme.common.dto.ApiResponse;
import com.govscheme.common.exception.ResourceNotFoundException;
import com.govscheme.matching.dto.MatchResponse;
import com.govscheme.matching.dto.MatchSummaryResponse;
import com.govscheme.matching.entity.UserSchemeMatch;
import com.govscheme.matching.entity.UserSchemeMatchRepository;
import com.govscheme.matching.service.MatchingService;
import com.govscheme.scheme.entity.Scheme;
import com.govscheme.scheme.entity.SchemeRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
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
    private final MatchingService matchingService;

    public MatchesController(UserRepository userRepository,
                             UserSchemeMatchRepository matchRepository,
                             SchemeRepository schemeRepository,
                             MatchingService matchingService) {
        this.userRepository = userRepository;
        this.matchRepository = matchRepository;
        this.schemeRepository = schemeRepository;
        this.matchingService = matchingService;
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

    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<MatchSummaryResponse>> summary(
            @AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(ApiResponse.success(summarize(userIdFor(principal.getUsername()))));
    }

    /**
     * Recomputes the caller's verdicts on demand. Stale rows (e.g. written
     * before a rule change) refresh here without waiting for the next
     * profile write or scheme sync.
     */
    @PostMapping("/recalculate")
    public ResponseEntity<ApiResponse<MatchSummaryResponse>> recalculate(
            @AuthenticationPrincipal UserDetails principal) {
        String userId = userIdFor(principal.getUsername());
        matchingService.recalculateForUser(userId);
        return ResponseEntity.ok(ApiResponse.success(summarize(userId), "Matches recalculated"));
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

    private MatchSummaryResponse summarize(String userId) {
        MatchSummaryResponse summary = new MatchSummaryResponse();
        summary.setTotal(matchRepository.countByUserId(userId));
        summary.setEligible(matchRepository.countByUserIdAndStatus(userId, UserSchemeMatch.Status.ELIGIBLE));
        summary.setInsufficientInformation(
            matchRepository.countByUserIdAndStatus(userId, UserSchemeMatch.Status.INSUFFICIENT_INFORMATION));
        summary.setNotEligible(
            matchRepository.countByUserIdAndStatus(userId, UserSchemeMatch.Status.NOT_ELIGIBLE));
        return summary;
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
