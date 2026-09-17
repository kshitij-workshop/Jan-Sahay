package com.govscheme.scheme.sync;

import com.govscheme.auth.TestMailConfig;
import com.govscheme.scheme.client.MySchemeClient;
import com.govscheme.scheme.client.MySchemeException;
import com.govscheme.scheme.client.MySchemeProperties;
import com.govscheme.scheme.entity.SchemeApplicationStepRepository;
import com.govscheme.scheme.entity.SchemeDocumentRepository;
import com.govscheme.scheme.entity.SchemeFaqRepository;
import com.govscheme.scheme.entity.SchemeRawDataRepository;
import com.govscheme.scheme.entity.SchemeRepository;
import com.govscheme.scheme.entity.SchemeStateRepository;
import com.govscheme.scheme.entity.SchemeTagRepository;
import com.govscheme.scheme.entity.SyncErrorRepository;
import com.govscheme.scheme.entity.SyncJob;
import com.govscheme.scheme.entity.SyncJobRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestMailConfig.class)
class SchemeSyncServiceTest {

    @Autowired
    private SchemeUpsertService upsertService;

    @Autowired
    private SchemeRepository schemeRepository;

    @Autowired
    private SchemeRawDataRepository rawDataRepository;

    @Autowired
    private SchemeTagRepository tagRepository;

    @Autowired
    private SchemeStateRepository stateRepository;

    @Autowired
    private SchemeFaqRepository faqRepository;

    @Autowired
    private SchemeDocumentRepository documentRepository;

    @Autowired
    private SchemeApplicationStepRepository stepRepository;

    @Autowired
    private SyncJobRepository jobRepository;

    @Autowired
    private SyncErrorRepository errorRepository;

    @BeforeEach
    void clean() {
        errorRepository.deleteAll();
        jobRepository.deleteAll();
        rawDataRepository.deleteAll();
        tagRepository.deleteAll();
        stateRepository.deleteAll();
        faqRepository.deleteAll();
        documentRepository.deleteAll();
        stepRepository.deleteAll();
        schemeRepository.deleteAll();
    }

    private SchemeSyncService service(MySchemeClient client, int maxSync) {
        MySchemeProperties properties = new MySchemeProperties();
        properties.setMaxSyncSchemes(maxSync);
        properties.setDetailConcurrency(2);
        return new SchemeSyncService(client, properties, upsertService, jobRepository, errorRepository);
    }

    @Test
    void syncCreatesSchemesWithRawAndChildren() {
        SyncJob job = service(new FlakyClient(false), 10).runSync("en", 0, null);

        assertThat(job.getStatus()).isEqualTo(SyncJob.Status.SUCCESS);
        assertThat(job.getFetched()).isEqualTo(2);
        assertThat(job.getCreatedCount()).isEqualTo(2);
        assertThat(job.getFailedCount()).isEqualTo(0);
        assertThat(schemeRepository.count()).isEqualTo(2);
        assertThat(rawDataRepository.count()).isEqualTo(2);
        assertThat(rawDataRepository.findBySlug("pm-kisan-demo")).isPresent();
        assertThat(tagRepository.count()).isEqualTo(1);
        assertThat(stateRepository.count()).isEqualTo(2);
        assertThat(faqRepository.count()).isEqualTo(2);
        assertThat(documentRepository.count()).isEqualTo(2);
        assertThat(stepRepository.count()).isEqualTo(2);
    }

    @Test
    void rerunIsIdempotentAndCountsUpdates() {
        SchemeSyncService sync = service(new FlakyClient(false), 10);
        sync.runSync("en", 0, null);

        SyncJob second = sync.runSync("en", 0, null);

        assertThat(second.getStatus()).isEqualTo(SyncJob.Status.SUCCESS);
        assertThat(second.getCreatedCount()).isEqualTo(0);
        assertThat(second.getUpdatedCount()).isEqualTo(2);
        assertThat(schemeRepository.count()).isEqualTo(2);
        assertThat(errorRepository.count()).isEqualTo(0);
    }

    @Test
    void limitCapsFetchedSchemes() {
        SyncJob job = service(new FlakyClient(false), 10).runSync("en", 0, 1);

        assertThat(job.getStatus()).isEqualTo(SyncJob.Status.SUCCESS);
        assertThat(job.getFetched()).isEqualTo(1);
        assertThat(schemeRepository.count()).isEqualTo(1);
    }

    @Test
    void failuresAreRecordedAndJobIsPartial() {
        SyncJob job = service(new FlakyClient(true), 10).runSync("en", 0, null);

        assertThat(job.getStatus()).isEqualTo(SyncJob.Status.PARTIAL);
        assertThat(job.getCreatedCount()).isEqualTo(1);
        assertThat(job.getFailedCount()).isEqualTo(1);
        assertThat(errorRepository.count()).isEqualTo(1);
        assertThat(errorRepository.findAll().get(0).getSlug()).isEqualTo("bihar-student-demo");
    }

    /**
     * Minimal fake: two slugs, optional detail failure on the second.
     */
    static class FlakyClient implements MySchemeClient {

        private final boolean failSecond;

        FlakyClient(boolean failSecond) {
            this.failSecond = failSecond;
        }

        @Override
        public SchemeSearchPage searchSchemes(String lang, int from, int size) {
            List<SchemeSummary> all = List.of(
                new SchemeSummary("pm-kisan-demo", "demo-1", "{}"),
                new SchemeSummary("bihar-student-demo", "demo-2", "{}"));
            int start = Math.min(from, all.size());
            int end = Math.min(start + size, all.size());
            return new SchemeSearchPage(all.subList(start, end), all.size());
        }

        @Override
        public String fetchSchemeDetailJson(String slug, String lang) {
            if (failSecond && slug.equals("bihar-student-demo")) {
                throw new MySchemeException("boom", 500, true);
            }
            if (slug.equals("pm-kisan-demo")) {
                return """
                    {"slug":"pm-kisan-demo","schemeName":"PM Kisan Demo",
                    "schemeCategory":"Agriculture","beneficiaryState":"Bihar",
                    "tags":["farmer"],"applicationProcess":"Step one\\nStep two"}""";
            }
            return """
                {"slug":"bihar-student-demo","schemeName":"Bihar Student Demo",
                "schemeCategory":"Education","beneficiaryState":"Bihar","tags":[]}""";
        }

        @Override
        public String fetchSchemeFaqsJson(String schemeId, String lang) {
            return """
                {"faqs":[{"question":"Who?","answer":"Farmers"}]}""";
        }

        @Override
        public String fetchSchemeDocumentsJson(String schemeId, String lang) {
            return """
                {"documents":[{"name":"Aadhaar Card","required":true}]}""";
        }
    }
}
