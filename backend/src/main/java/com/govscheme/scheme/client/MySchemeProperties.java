package com.govscheme.scheme.client;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.myscheme")
public class MySchemeProperties {

    private String apiKey;
    private String baseUrl = "https://api.myscheme.gov.in";
    private int connectTimeoutMs = 10000;
    private int readTimeoutMs = 30000;
    private int maxSyncSchemes = 50;
    private int detailConcurrency = 4;
    private int maxAttempts = 3;

    public String getApiKey() { return apiKey; }
    public void setApiKey(String apiKey) { this.apiKey = apiKey; }

    public String getBaseUrl() { return baseUrl; }
    public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }

    public int getConnectTimeoutMs() { return connectTimeoutMs; }
    public void setConnectTimeoutMs(int connectTimeoutMs) { this.connectTimeoutMs = connectTimeoutMs; }

    public int getReadTimeoutMs() { return readTimeoutMs; }
    public void setReadTimeoutMs(int readTimeoutMs) { this.readTimeoutMs = readTimeoutMs; }

    public int getMaxSyncSchemes() { return maxSyncSchemes; }
    public void setMaxSyncSchemes(int maxSyncSchemes) { this.maxSyncSchemes = maxSyncSchemes; }

    public int getDetailConcurrency() { return detailConcurrency; }
    public void setDetailConcurrency(int detailConcurrency) { this.detailConcurrency = detailConcurrency; }

    public int getMaxAttempts() { return maxAttempts; }
    public void setMaxAttempts(int maxAttempts) { this.maxAttempts = maxAttempts; }

    public boolean hasApiKey() {
        return apiKey != null && !apiKey.isBlank();
    }
}
