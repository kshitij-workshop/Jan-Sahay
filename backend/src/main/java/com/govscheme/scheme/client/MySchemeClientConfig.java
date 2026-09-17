package com.govscheme.scheme.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Selects the myScheme client: live HTTP when an API key is configured,
 * otherwise the offline stub so demo mode and tests work without credentials.
 * The key itself never leaves the backend.
 */
@Configuration
public class MySchemeClientConfig {

    private static final Logger log = LoggerFactory.getLogger(MySchemeClientConfig.class);

    @Bean
    public MySchemeClient mySchemeClient(MySchemeProperties properties, ObjectMapper objectMapper) {
        if (properties.hasApiKey()) {
            log.info("MYSCHEME_CLIENT mode=live baseUrl={}", properties.getBaseUrl());
            return new RestMySchemeClient(properties, objectMapper);
        }
        log.warn("MYSCHEME_CLIENT mode=stub (no MYSCHEME_API_KEY configured)");
        return new StubMySchemeClient();
    }
}
