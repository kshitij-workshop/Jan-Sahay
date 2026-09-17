package com.govscheme.scheme;

import com.govscheme.auth.TestMailConfig;
import com.govscheme.scheme.entity.Scheme;
import com.govscheme.scheme.entity.SchemeCategory;
import com.govscheme.scheme.entity.SchemeCategoryRepository;
import com.govscheme.scheme.entity.SchemeDocument;
import com.govscheme.scheme.entity.SchemeDocumentRepository;
import com.govscheme.scheme.entity.SchemeFaq;
import com.govscheme.scheme.entity.SchemeFaqRepository;
import com.govscheme.scheme.entity.SchemeRepository;
import com.govscheme.scheme.entity.SchemeState;
import com.govscheme.scheme.entity.SchemeStateRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestMailConfig.class)
class SchemeControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SchemeRepository schemeRepository;

    @Autowired
    private SchemeCategoryRepository categoryRepository;

    @Autowired
    private SchemeStateRepository stateRepository;

    @Autowired
    private SchemeFaqRepository faqRepository;

    @Autowired
    private SchemeDocumentRepository documentRepository;

    private String scholarshipId;

    @BeforeEach
    void seed() {
        documentRepository.deleteAll();
        faqRepository.deleteAll();
        stateRepository.deleteAll();
        categoryRepository.deleteAll();
        schemeRepository.deleteAll();

        Scheme farming = scheme("pm-kisan", "PM Kisan", "Agriculture", "All India", "Central");
        farming.setBriefDescriptionEng("Income support for farmers");
        schemeRepository.save(farming);

        Scheme scholarship = scheme("bihar-scholarship", "Bihar Scholarship", null, "Bihar", "State");
        scholarship.setBriefDescriptionEng("Scholarship for Bihar students");
        schemeRepository.save(scholarship);
        scholarshipId = scholarship.getId();

        SchemeCategory category = new SchemeCategory();
        category.setSchemeId(scholarship.getId());
        category.setCategory("Education");
        category.setKind(SchemeCategory.KIND_MAIN);
        categoryRepository.save(category);

        SchemeState state = new SchemeState();
        state.setSchemeId(scholarship.getId());
        state.setState("Bihar");
        stateRepository.save(state);

        SchemeFaq faq = new SchemeFaq();
        faq.setSchemeId(scholarship.getId());
        faq.setQuestion("Who?");
        faq.setAnswer("Students");
        faq.setPosition(0);
        faqRepository.save(faq);

        SchemeDocument document = new SchemeDocument();
        document.setSchemeId(scholarship.getId());
        document.setName("Aadhaar Card");
        document.setRequired(true);
        document.setPosition(0);
        documentRepository.save(document);
    }

    private Scheme scheme(String slug, String name, String category, String state, String level) {
        Scheme scheme = new Scheme();
        scheme.setSlug(slug);
        scheme.setSchemeName(name);
        scheme.setSchemeCategory(category);
        scheme.setBeneficiaryState(state);
        scheme.setLevel(level);
        scheme.setSource("TEST");
        return scheme;
    }

    @Test
    void listingIsPublicAndPaginated() throws Exception {
        mockMvc.perform(get("/api/schemes").param("size", "1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.totalElements").value(2))
            .andExpect(jsonPath("$.data.content.length()").value(1));
    }

    @Test
    void textSearchMatchesNameAndDescription() throws Exception {
        mockMvc.perform(get("/api/schemes").param("q", "scholarship"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.totalElements").value(1))
            .andExpect(jsonPath("$.data.content[0].slug").value("bihar-scholarship"));

        mockMvc.perform(get("/api/schemes").param("q", "FARMERS"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.totalElements").value(1));
    }

    @Test
    void categoryFilterFindsChildTableRows() throws Exception {
        mockMvc.perform(get("/api/schemes").param("category", "Education"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.totalElements").value(1))
            .andExpect(jsonPath("$.data.content[0].slug").value("bihar-scholarship"));
    }

    @Test
    void stateFilterMatchesColumnAndChildRows() throws Exception {
        mockMvc.perform(get("/api/schemes").param("state", "Bihar"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.totalElements").value(2));

        // All-India schemes apply in every state, so Kerala still matches one.
        mockMvc.perform(get("/api/schemes").param("state", "Kerala"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.totalElements").value(1))
            .andExpect(jsonPath("$.data.content[0].slug").value("pm-kisan"));
    }

    @Test
    void detailReturnsSectionsAndProvenance() throws Exception {
        mockMvc.perform(get("/api/schemes/" + scholarshipId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.slug").value("bihar-scholarship"))
            .andExpect(jsonPath("$.data.faqs.length()").value(1))
            .andExpect(jsonPath("$.data.faqs[0].question").value("Who?"))
            .andExpect(jsonPath("$.data.documents[0].name").value("Aadhaar Card"))
            .andExpect(jsonPath("$.data.categories[0].category").value("Education"))
            .andExpect(jsonPath("$.data.source").value("TEST"));
    }

    @Test
    void unknownIdReturns404() throws Exception {
        mockMvc.perform(get("/api/schemes/does-not-exist"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.error").value("NOT_FOUND"));
    }
}
