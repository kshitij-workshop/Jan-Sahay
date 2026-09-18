package com.govscheme.matching.dto;

import com.govscheme.matching.entity.UserSchemeMatch;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

public class MatchResponse {

    private String schemeId;
    private String schemeName;
    private String schemeSlug;
    private UserSchemeMatch.Status status;
    private String matchReason;
    private List<String> missingInformation;
    private Instant firstMatchedAt;
    private Instant lastCheckedAt;

    public static MatchResponse of(UserSchemeMatch match, String schemeName, String schemeSlug) {
        MatchResponse dto = new MatchResponse();
        dto.setSchemeId(match.getSchemeId());
        dto.setSchemeName(schemeName);
        dto.setSchemeSlug(schemeSlug);
        dto.setStatus(match.getStatus());
        dto.setMatchReason(match.getMatchReason());
        dto.setMissingInformation(match.getMissingInformation() == null
            || match.getMissingInformation().isBlank()
            ? List.of()
            : Arrays.asList(match.getMissingInformation().split(",")));
        dto.setFirstMatchedAt(match.getFirstMatchedAt());
        dto.setLastCheckedAt(match.getLastCheckedAt());
        return dto;
    }

    public String getSchemeId() { return schemeId; }
    public void setSchemeId(String schemeId) { this.schemeId = schemeId; }

    public String getSchemeName() { return schemeName; }
    public void setSchemeName(String schemeName) { this.schemeName = schemeName; }

    public String getSchemeSlug() { return schemeSlug; }
    public void setSchemeSlug(String schemeSlug) { this.schemeSlug = schemeSlug; }

    public UserSchemeMatch.Status getStatus() { return status; }
    public void setStatus(UserSchemeMatch.Status status) { this.status = status; }

    public String getMatchReason() { return matchReason; }
    public void setMatchReason(String matchReason) { this.matchReason = matchReason; }

    public List<String> getMissingInformation() { return missingInformation; }
    public void setMissingInformation(List<String> missingInformation) { this.missingInformation = missingInformation; }

    public Instant getFirstMatchedAt() { return firstMatchedAt; }
    public void setFirstMatchedAt(Instant firstMatchedAt) { this.firstMatchedAt = firstMatchedAt; }

    public Instant getLastCheckedAt() { return lastCheckedAt; }
    public void setLastCheckedAt(Instant lastCheckedAt) { this.lastCheckedAt = lastCheckedAt; }
}
