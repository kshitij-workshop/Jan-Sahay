package com.govscheme.scheme.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;

/**
 * Original myScheme payload, preserved verbatim. The normalized {@link Scheme}
 * row is derived from this and must never invent fields the payload lacks.
 */
@Entity
@Table(name = "scheme_raw_data")
public class SchemeRawData {

    @Id
    @Column(name = "scheme_id", updatable = false, nullable = false, columnDefinition = "CHAR(36)")
    private String schemeId;

    @Column(name = "slug", nullable = false, length = 255)
    private String slug;

    @Column(name = "kind", nullable = false, length = 30)
    private String kind = "DETAIL";

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "payload", nullable = false, columnDefinition = "JSON")
    private String payload;

    @CreationTimestamp
    @Column(name = "fetched_at", nullable = false, updatable = false)
    private Instant fetchedAt;

    public String getSchemeId() { return schemeId; }
    public void setSchemeId(String schemeId) { this.schemeId = schemeId; }

    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }

    public String getKind() { return kind; }
    public void setKind(String kind) { this.kind = kind; }

    public String getPayload() { return payload; }
    public void setPayload(String payload) { this.payload = payload; }

    public Instant getFetchedAt() { return fetchedAt; }
    public void setFetchedAt(Instant fetchedAt) { this.fetchedAt = fetchedAt; }
}
