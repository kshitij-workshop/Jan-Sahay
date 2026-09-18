package com.govscheme.scheme.sync;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.govscheme.auth.TestMailConfig;
import com.govscheme.matching.service.MatchingService;
import com.govscheme.scheme.entity.SchemeBeneficiaryRepository;
import com.govscheme.scheme.entity.SchemeCategoryRepository;
import com.govscheme.scheme.entity.SchemeDocumentRepository;
import com.govscheme.scheme.entity.SchemeFaqRepository;
import com.govscheme.scheme.entity.SchemeRawDataRepository;
import com.govscheme.scheme.entity.SchemeReferenceRepository;
import com.govscheme.scheme.entity.SchemeRepository;
import com.govscheme.scheme.entity.SyncErrorRepository;
import com.govscheme.scheme.entity.SyncJob;
import com.govscheme.scheme.entity.SyncJobRepository;
import com.govscheme.scheme.entity.SchemeApplicationStepRepository;
import com.govscheme.scheme.entity.SchemeStateRepository;
import com.govscheme.scheme.entity.SchemeTagRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestMailConfig.class)
@Transactional
class SchemeFileImportServiceTest {

    @Autowired
    private SchemeUpsertService upsertService;

    @Autowired
    private SyncJobRepository jobRepository;

    @Autowired
    private SyncErrorRepository errorRepository;

    @Autowired
    private SchemeRepository schemeRepository;

    @Autowired
    private SchemeRawDataRepository rawDataRepository;

    @Autowired
    private SchemeCategoryRepository categoryRepository;

    @Autowired
    private SchemeBeneficiaryRepository beneficiaryRepository;

    @Autowired
    private SchemeFaqRepository faqRepository;

    @Autowired
    private SchemeDocumentRepository documentRepository;

    @Autowired
    private SchemeReferenceRepository referenceRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SchemeTagRepository tagRepository;

    @Autowired
    private SchemeStateRepository stateRepository;

    @Autowired
    private SchemeApplicationStepRepository stepRepository;
    @TempDir
    private Path tempDir;

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
        categoryRepository.deleteAll();
        beneficiaryRepository.deleteAll();
        referenceRepository.deleteAll();
        schemeRepository.deleteAll();
    }

    @Autowired
    private MatchingService matchingService;

    private SchemeFileImportService service(Path file) {
        return new SchemeFileImportService(upsertService, matchingService, jobRepository, errorRepository,
            objectMapper, file.toString());
    }

    private Path writeFixture(String name, String content) throws Exception {
        Path file = tempDir.resolve(name);
        Files.writeString(file, content);
        return file;
    }

    private static final String FULL_ITEM = """
        {"slug":"demo-import","id":"abc123","sourceUrl":"https://example.com/demo",
        "level":"Central","schemeName":"Demo Scheme","shortTitle":"DEMO",
        "schemeType":"Central Sector Scheme","ministry":"Ministry of Demo",
        "department":"Department of Demo","benefitType":"Cash",
        "briefDescription":"Brief","detailedDescription_md":"Detailed",
        "benefits_md":"Benefits","eligibility_md":"Eligibility","exclusions_md":"Exclusions",
        "documents_md":"Aadhaar Card",
        "categories":["Education"],"subCategories":["Scholarship"],
        "beneficiaries":["Student"],"tags":["study"],"schemeFor":"Student",
        "faqs":[{"question":"Who?","answer_md":"Students"}],
        "documents_raw":[{"type":"paragraph","children":[{"text":"a. Aadhaar Card"}]},
        {"type":"paragraph","children":[{"text":"Essential Documents:"}]}],
        "references":[{"title":"Guidelines","url":"https://example.com/guide.pdf"}],
        "applicationProcess":[{"mode":"Online","url":"","process_md":"**Step 1:** Register\\n**Step 2:** Apply"}]}""";

    @Test
    void importsFileItemsWithNormalizedChildren() throws Exception {
        Path file = writeFixture("schemes.json",
            "[" + FULL_ITEM + ",{\"slug\":\"demo-minimal\"},{}]");

        SyncJob job = service(file).importFile();

        assertThat(job.getStatus()).isEqualTo(SyncJob.Status.PARTIAL);
        assertThat(job.getFetched()).isEqualTo(3);
        assertThat(job.getCreatedCount()).isEqualTo(2);
        assertThat(job.getFailedCount()).isEqualTo(1);
        assertThat(errorRepository.count()).isEqualTo(1);

        assertThat(schemeRepository.findBySlug("demo-import")).isPresent();
        var scheme = schemeRepository.findBySlug("demo-import").orElseThrow();
        assertThat(scheme.getBenefitsMd()).isEqualTo("Benefits");
        assertThat(scheme.getBenefitType()).isEqualTo("Cash");
        assertThat(scheme.getExternalId()).isEqualTo("abc123");
        assertThat(scheme.getSource()).isEqualTo("FILE");
        assertThat(rawDataRepository.findBySlug("demo-import")).isPresent();
        assertThat(categoryRepository.findBySchemeId(scheme.getId())).hasSize(2);
        assertThat(beneficiaryRepository.findBySchemeId(scheme.getId())).hasSize(1);
        assertThat(faqRepository.findBySchemeIdOrderByPositionAsc(scheme.getId())).hasSize(1);
        assertThat(documentRepository.findBySchemeIdOrderByPositionAsc(scheme.getId())).hasSize(1);
        assertThat(referenceRepository.findBySchemeIdOrderByPositionAsc(scheme.getId())).hasSize(1);
        assertThat(schemeRepository.findBySlug("demo-minimal")).isPresent();
    }

    @Test
    void rerunUpdatesWithoutDuplicates() throws Exception {
        Path file = writeFixture("schemes.json", "[" + FULL_ITEM + "]");
        SchemeFileImportService importer = service(file);
        importer.importFile();

        SyncJob second = importer.importFile();

        assertThat(second.getStatus()).isEqualTo(SyncJob.Status.SUCCESS);
        assertThat(second.getCreatedCount()).isEqualTo(0);
        assertThat(second.getUpdatedCount()).isEqualTo(1);
        assertThat(schemeRepository.count()).isEqualTo(1);
    }

    @Test
    void missingFileFailsJob() {
        SyncJob job = service(tempDir.resolve("absent.json")).importFile();

        assertThat(job.getStatus()).isEqualTo(SyncJob.Status.FAILED);
        assertThat(job.getErrorMessage()).contains("Cannot read import file");
    }
}
