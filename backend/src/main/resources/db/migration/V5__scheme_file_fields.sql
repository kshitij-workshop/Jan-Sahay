-- V5__scheme_file_fields.sql
-- Columns/tables for fields found in offline scheme dumps
-- (data/schemes.json): markdown content, benefit metadata, categories,
-- beneficiaries and references. All nullable: normalization stays best-effort
-- and the verbatim payload in scheme_raw_data remains authoritative.

ALTER TABLE schemes
ADD COLUMN eligibility_md TEXT NULL,
ADD COLUMN benefits_md TEXT NULL,
ADD COLUMN exclusions_md TEXT NULL,
ADD COLUMN detailed_description_md TEXT NULL,
ADD COLUMN documents_md TEXT NULL,
ADD COLUMN benefit_type VARCHAR(100) NULL,
ADD COLUMN scheme_type VARCHAR(255) NULL,
ADD COLUMN department VARCHAR(255) NULL,
ADD COLUMN open_date VARCHAR(50) NULL,
ADD COLUMN image_url VARCHAR(1000) NULL,
ADD COLUMN external_id VARCHAR(100) NULL;

CREATE INDEX idx_schemes_external_id ON schemes(external_id);

CREATE TABLE scheme_categories (
    id CHAR(36) NOT NULL PRIMARY KEY,
    scheme_id CHAR(36) NOT NULL,
    category VARCHAR(255) NOT NULL,
    kind VARCHAR(10) NOT NULL DEFAULT 'MAIN',
    CONSTRAINT fk_categories_scheme FOREIGN KEY (scheme_id) REFERENCES schemes (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_categories_scheme ON scheme_categories(scheme_id);
CREATE INDEX idx_categories_category ON scheme_categories(category);

CREATE TABLE scheme_beneficiaries (
    id CHAR(36) NOT NULL PRIMARY KEY,
    scheme_id CHAR(36) NOT NULL,
    beneficiary VARCHAR(255) NOT NULL,
    CONSTRAINT fk_beneficiaries_scheme FOREIGN KEY (scheme_id) REFERENCES schemes (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_beneficiaries_scheme ON scheme_beneficiaries(scheme_id);
CREATE INDEX idx_beneficiaries_beneficiary ON scheme_beneficiaries(beneficiary);

CREATE TABLE scheme_references (
    id CHAR(36) NOT NULL PRIMARY KEY,
    scheme_id CHAR(36) NOT NULL,
    title VARCHAR(500) NOT NULL,
    url VARCHAR(1000) NOT NULL,
    position INT NOT NULL DEFAULT 0,
    CONSTRAINT fk_references_scheme FOREIGN KEY (scheme_id) REFERENCES schemes (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_references_scheme ON scheme_references(scheme_id);
