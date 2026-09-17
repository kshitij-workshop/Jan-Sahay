package com.govscheme.scheme.client;

import java.util.List;
import java.util.Map;

/**
 * Offline stand-in used when no {@code MYSCHEME_API_KEY} is configured and in
 * tests. Payload shapes mirror the documented myScheme fields (slug,
 * schemeName, beneficiaryState, tags, …) so normalization is exercised
 * without network access. Clearly synthetic data.
 */
public class StubMySchemeClient implements MySchemeClient {

    private final List<SchemeSummary> catalogue = List.of(
        new SchemeSummary("pm-kisan-demo", "demo-1", """
            {"slug":"pm-kisan-demo","schemeId":"demo-1","schemeName":"PM Kisan Demo",
            "schemeNameEng":"PM Kisan Demo","schemeShortTitle":"PMK-DEMO","schemeCategory":"Agriculture",
            "beneficiaryState":"All India","level":"Central","schemeFor":"Farmer",
            "nodalMinistryName":"Ministry of Agriculture","briefDescription":"Demo income support",
            "briefDescriptionEng":"Demo income support","priority":1,
            "tags":["farmer","income"],"tagsEng":["farmer","income"]}"""),
        new SchemeSummary("bihar-student-demo", "demo-2", """
            {"slug":"bihar-student-demo","schemeId":"demo-2","schemeName":"Bihar Student Demo",
            "schemeNameEng":"Bihar Student Demo","schemeShortTitle":"BSD-DEMO","schemeCategory":"Education",
            "beneficiaryState":"Bihar","level":"State","schemeFor":"Student",
            "nodalMinistryName":"Education Department, Bihar","briefDescription":"Demo scholarship",
            "briefDescriptionEng":"Demo scholarship","priority":2,
            "tags":["student","scholarship"],"tagsEng":["student","scholarship"]}""")
    );

    @Override
    public SchemeSearchPage searchSchemes(String lang, int from, int size) {
        int start = Math.min(Math.max(0, from), catalogue.size());
        int end = Math.min(start + Math.max(0, size), catalogue.size());
        return new SchemeSearchPage(catalogue.subList(start, end), catalogue.size());
    }

    @Override
    public String fetchSchemeDetailJson(String slug, String lang) {
        return catalogue.stream()
            .filter(s -> s.slug().equals(slug))
            .map(SchemeSummary::rawJson)
            .findFirst()
            .orElseThrow(() -> new MySchemeException("Unknown demo slug: " + slug, 404, false));
    }

    @Override
    public String fetchSchemeFaqsJson(String schemeId, String lang) {
        return """
            {"faqs":[{"question":"Who can apply?","answer":"See eligibility."},
            {"question":"Where to apply?","answer":"Official portal."}]}""";
    }

    @Override
    public String fetchSchemeDocumentsJson(String schemeId, String lang) {
        return """
            {"documents":[{"name":"Aadhaar Card","required":true},
            {"name":"Income Certificate","required":true}]}""";
    }

    public Map<String, Integer> catalogueSize() {
        return Map.of("total", catalogue.size());
    }
}
