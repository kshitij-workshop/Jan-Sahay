package com.govscheme.scheme.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * Live myScheme.gov.in client. Retries 429/5xx and transport failures with
 * exponential backoff; other 4xx responses fail fast as permanent errors.
 */
public class RestMySchemeClient implements MySchemeClient {

    private static final Logger log = LoggerFactory.getLogger(RestMySchemeClient.class);

    private final MySchemeProperties properties;
    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public RestMySchemeClient(MySchemeProperties properties, ObjectMapper objectMapper) {
        this(properties, objectMapper, buildDefaultClient(properties));
    }

    RestMySchemeClient(MySchemeProperties properties, ObjectMapper objectMapper, RestClient restClient) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.restClient = restClient;
    }

    private static RestClient buildDefaultClient(MySchemeProperties properties) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofMillis(properties.getConnectTimeoutMs()));
        factory.setReadTimeout(Duration.ofMillis(properties.getReadTimeoutMs()));
        return RestClient.builder()
            .baseUrl(properties.getBaseUrl())
            .requestFactory(factory)
            .defaultHeader("x-api-key", properties.getApiKey() == null ? "" : properties.getApiKey())
            .build();
    }

    @Override
    public SchemeSearchPage searchSchemes(String lang, int from, int size) {
        String body = getWithRetry("/search/v6/schemes?lang=" + lang
            + "&q=[]&keyword=&sort=&from=" + from + "&size=" + size, "SEARCH", null);
        return parseSearchPage(body);
    }

    @Override
    public String fetchSchemeDetailJson(String slug, String lang) {
        return getWithRetry("/schemes/v6/public/schemes?slug=" + slug + "&lang=" + lang, "DETAIL", slug);
    }

    @Override
    public String fetchSchemeFaqsJson(String schemeId, String lang) {
        try {
            return getWithRetry("/schemes/v6/public/schemes/" + schemeId + "/faqs?lang=" + lang, "FAQS", schemeId);
        } catch (MySchemeException e) {
            // FAQs are enrichment, not critical: 404/absent means "none published".
            if (!e.isRetryable() && e.getStatusCode() == 404) {
                return null;
            }
            throw e;
        }
    }

    @Override
    public String fetchSchemeDocumentsJson(String schemeId, String lang) {
        try {
            return getWithRetry("/schemes/v6/public/schemes/" + schemeId + "/documents?lang=" + lang, "DOCUMENTS", schemeId);
        } catch (MySchemeException e) {
            if (!e.isRetryable() && e.getStatusCode() == 404) {
                return null;
            }
            throw e;
        }
    }

    private String getWithRetry(String path, String stage, String ref) {
        int attempts = Math.max(1, properties.getMaxAttempts());
        MySchemeException last = null;
        for (int attempt = 1; attempt <= attempts; attempt++) {
            try {
                String body = restClient.get().uri(path).retrieve()
                    .onStatus(HttpStatusCode::isError, (request, response) -> {
                        int code = response.getStatusCode().value();
                        boolean retryable = code == 429 || code >= 500;
                        throw new MySchemeException(
                            "myScheme " + stage + " failed with HTTP " + code, code, retryable);
                    })
                    .body(String.class);
                if (attempt > 1) {
                    log.info("MYSCHEME_RETRY_OK stage={} ref={} attempt={}", stage, ref, attempt);
                }
                return body == null ? "{}" : body;
            } catch (MySchemeException e) {
                last = e;
                if (!e.isRetryable() || attempt == attempts) {
                    throw e;
                }
                backoff(stage, ref, attempt);
            } catch (ResourceAccessException e) {
                last = new MySchemeException("myScheme " + stage + " transport failure", 0, true, e);
                if (attempt == attempts) {
                    throw last;
                }
                backoff(stage, ref, attempt);
            }
        }
        throw last;
    }

    private void backoff(String stage, String ref, int attempt) {
        long delayMs = 1000L << (attempt - 1); // 1s, 2s, 4s, ...
        log.warn("MYSCHEME_RETRY stage={} ref={} attempt={} delayMs={}", stage, ref, attempt, delayMs);
        try {
            Thread.sleep(delayMs);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            throw new MySchemeException("myScheme retry interrupted", 0, false, ie);
        }
    }

    SchemeSearchPage parseSearchPage(String body) {
        try {
            JsonNode root = objectMapper.readTree(body);
            List<SchemeSummary> items = new ArrayList<>();
            int total = 0;
            for (JsonNode list : candidateLists(root)) {
                for (JsonNode item : list) {
                    String slug = text(item, "slug");
                    if (slug == null || slug.isBlank()) {
                        continue;
                    }
                    String schemeId = text(item, "schemeId");
                    if (schemeId == null) {
                        schemeId = text(item, "id");
                    }
                    items.add(new SchemeSummary(slug, schemeId, item.toString()));
                }
            }
            JsonNode totalNode = firstPresent(root, "total", "totalCount", "count", "totalElements");
            if (totalNode != null && totalNode.isNumber()) {
                total = totalNode.asInt();
            } else {
                total = items.size();
            }
            return new SchemeSearchPage(items, total);
        } catch (Exception e) {
            throw new MySchemeException("myScheme SEARCH response not parseable", 0, false, e);
        }
    }

    private List<JsonNode> candidateLists(JsonNode root) {
        List<JsonNode> lists = new ArrayList<>();
        if (root.isArray()) {
            lists.add(root);
            return lists;
        }
        for (String key : new String[]{"schemes", "hits", "results", "items", "data"}) {
            JsonNode node = root.get(key);
            if (node == null || node.isNull()) {
                continue;
            }
            if (node.isArray()) {
                lists.add(node);
            } else if (node.isObject()) {
                for (JsonNode inner : candidateLists(node)) {
                    lists.add(inner);
                }
            }
        }
        return lists;
    }

    private JsonNode firstPresent(JsonNode root, String... keys) {
        for (String key : keys) {
            JsonNode node = root.get(key);
            if (node != null && !node.isNull()) {
                return node;
            }
            if (root.has("data") && root.get("data").isObject()) {
                JsonNode inner = root.get("data").get(key);
                if (inner != null && !inner.isNull()) {
                    return inner;
                }
            }
        }
        return null;
    }

    private String text(JsonNode node, String key) {
        JsonNode value = node.get(key);
        if (value == null || value.isNull()) {
            return null;
        }
        return value.asText();
    }
}
