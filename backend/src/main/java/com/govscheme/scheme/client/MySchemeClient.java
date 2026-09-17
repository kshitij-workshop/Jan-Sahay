package com.govscheme.scheme.client;

import java.util.List;

/**
 * Abstraction over the myScheme.gov.in API. Implementations return raw JSON
 * payloads; normalization into {@code schemes} tables lives in the sync
 * service so parsing rules stay in one place and stay testable.
 */
public interface MySchemeClient {

    /**
     * Paginated scheme search. {@code from} is a zero-based offset.
     */
    SchemeSearchPage searchSchemes(String lang, int from, int size);

    /**
     * Raw detail payload for a scheme slug (may be a JSON object or an
     * envelope wrapping one).
     */
    String fetchSchemeDetailJson(String slug, String lang);

    /**
     * Raw FAQ payload for a scheme id. Returns {@code null} when unavailable.
     */
    String fetchSchemeFaqsJson(String schemeId, String lang);

    /**
     * Raw required-documents payload for a scheme id. Returns {@code null}
     * when unavailable.
     */
    String fetchSchemeDocumentsJson(String schemeId, String lang);

    record SchemeSummary(String slug, String schemeId, String rawJson) {
    }

    record SchemeSearchPage(List<SchemeSummary> items, int total) {
    }
}
