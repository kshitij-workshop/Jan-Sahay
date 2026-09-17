-- V6__scheme_eligibility.sql
-- Structured, admin-curated eligibility criteria per scheme. Every column is
-- nullable: NULL means "this scheme does not constrain that dimension" and
-- the corresponding rule is skipped. Values here are the ONLY machine-readable
-- requirements the eligibility engine may enforce; free-text eligibility_md
-- is display-only and never parsed into verdicts.

CREATE TABLE scheme_eligibility (
    scheme_id CHAR(36) NOT NULL PRIMARY KEY,
    min_age INT NULL,
    max_age INT NULL,
    states TEXT NULL,
    genders VARCHAR(100) NULL,
    max_annual_income BIGINT NULL,
    caste_categories VARCHAR(255) NULL,
    occupations VARCHAR(500) NULL,
    require_student BOOLEAN NULL,
    require_disabled BOOLEAN NULL,
    min_disability_percentage INT NULL,
    area_types VARCHAR(50) NULL,
    employment_statuses VARCHAR(255) NULL,
    require_govt_employee BOOLEAN NULL,
    require_bpl BOOLEAN NULL,
    marital_statuses VARCHAR(255) NULL,
    require_minority BOOLEAN NULL,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_eligibility_scheme FOREIGN KEY (scheme_id) REFERENCES schemes (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
