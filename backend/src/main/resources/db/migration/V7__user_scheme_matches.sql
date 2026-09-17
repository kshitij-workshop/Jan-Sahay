-- V7__user_scheme_matches.sql
-- Persisted eligibility verdicts per user and scheme. Rows are rewritten by
-- MatchingService whenever the profile changes or schemes sync; notified_at
-- is reserved for the Phase 11 notification system.

CREATE TABLE user_scheme_matches (
    id CHAR(36) NOT NULL PRIMARY KEY,
    user_id CHAR(36) NOT NULL,
    scheme_id CHAR(36) NOT NULL,
    status VARCHAR(30) NOT NULL,
    match_reason TEXT,
    missing_information TEXT NULL,
    first_matched_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_checked_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    notified_at TIMESTAMP NULL,
    CONSTRAINT fk_matches_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_matches_scheme FOREIGN KEY (scheme_id) REFERENCES schemes (id) ON DELETE CASCADE,
    UNIQUE KEY uk_matches_user_scheme (user_id, scheme_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_matches_user_status ON user_scheme_matches(user_id, status);
CREATE INDEX idx_matches_scheme ON user_scheme_matches(scheme_id);
