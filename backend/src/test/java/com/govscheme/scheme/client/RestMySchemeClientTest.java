package com.govscheme.scheme.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class RestMySchemeClientTest {

    private static final String SEARCH_BODY = """
        {"total":2,"data":[{"slug":"alpha","schemeId":"1","schemeName":"Alpha"},
        {"slug":"beta","schemeId":"2","schemeName":"Beta"}]}""";

    private MySchemeProperties properties;
    private RestClient.Builder builder;
    private MockRestServiceServer server;
    private RestMySchemeClient client;

    @BeforeEach
    void setUp() {
        properties = new MySchemeProperties();
        properties.setApiKey("test-key");
        properties.setBaseUrl("https://api.myscheme.gov.in");
        properties.setConnectTimeoutMs(2000);
        properties.setReadTimeoutMs(2000);
        properties.setMaxAttempts(3);
        builder = RestClient.builder()
            .baseUrl("https://api.myscheme.gov.in")
            .defaultHeader("x-api-key", "test-key");
        server = MockRestServiceServer.bindTo(builder).build();
        client = new RestMySchemeClient(properties, new ObjectMapper(), builder.build());
    }

    @Test
    void searchSendsApiKeyAndParsesItems() {
        server.expect(requestTo("https://api.myscheme.gov.in/search/v6/schemes?lang=en&q=%5B%5D&keyword=&sort=&from=0&size=10"))
            .andExpect(header("x-api-key", "test-key"))
            .andRespond(withSuccess(SEARCH_BODY, MediaType.APPLICATION_JSON));

        MySchemeClient.SchemeSearchPage page = client.searchSchemes("en", 0, 10);

        assertThat(page.items()).hasSize(2);
        assertThat(page.items().get(0).slug()).isEqualTo("alpha");
        assertThat(page.items().get(1).schemeId()).isEqualTo("2");
        assertThat(page.total()).isEqualTo(2);
        server.verify();
    }

    @Test
    void retriesRateLimitThenSucceeds() {
        server.expect(requestTo("https://api.myscheme.gov.in/search/v6/schemes?lang=en&q=%5B%5D&keyword=&sort=&from=0&size=10"))
            .andRespond(withStatus(org.springframework.http.HttpStatus.TOO_MANY_REQUESTS));
        server.expect(requestTo("https://api.myscheme.gov.in/search/v6/schemes?lang=en&q=%5B%5D&keyword=&sort=&from=0&size=10"))
            .andRespond(withSuccess(SEARCH_BODY, MediaType.APPLICATION_JSON));

        MySchemeClient.SchemeSearchPage page = client.searchSchemes("en", 0, 10);

        assertThat(page.items()).hasSize(2);
        server.verify();
    }

    @Test
    void badRequestFailsWithoutRetry() {
        server.expect(requestTo("https://api.myscheme.gov.in/search/v6/schemes?lang=en&q=%5B%5D&keyword=&sort=&from=0&size=10"))
            .andRespond(withStatus(org.springframework.http.HttpStatus.BAD_REQUEST));

        assertThatThrownBy(() -> client.searchSchemes("en", 0, 10))
            .isInstanceOf(MySchemeException.class)
            .matches(e -> !((MySchemeException) e).isRetryable());
        server.verify();
    }

    @Test
    void missingFaqsReturnNullInsteadOfFailing() {
        server.expect(requestTo("https://api.myscheme.gov.in/schemes/v6/public/schemes/1/faqs?lang=en"))
            .andRespond(withStatus(org.springframework.http.HttpStatus.NOT_FOUND));

        assertThat(client.fetchSchemeFaqsJson("1", "en")).isNull();
        server.verify();
    }
}
