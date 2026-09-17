package com.govscheme.scheme.sync;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.govscheme.scheme.entity.Scheme;
import com.govscheme.scheme.entity.SchemeBeneficiary;
import com.govscheme.scheme.entity.SchemeBeneficiaryRepository;
import com.govscheme.scheme.entity.SchemeCategory;
import com.govscheme.scheme.entity.SchemeCategoryRepository;
import com.govscheme.scheme.entity.SchemeReference;
import com.govscheme.scheme.entity.SchemeReferenceRepository;
import com.govscheme.scheme.entity.SchemeApplicationStep;
import com.govscheme.scheme.entity.SchemeApplicationStepRepository;
import com.govscheme.scheme.entity.SchemeDocument;
import com.govscheme.scheme.entity.SchemeDocumentRepository;
import com.govscheme.scheme.entity.SchemeFaq;
import com.govscheme.scheme.entity.SchemeFaqRepository;
import com.govscheme.scheme.entity.SchemeRawData;
import com.govscheme.scheme.entity.SchemeRawDataRepository;
import com.govscheme.scheme.entity.SchemeRepository;
import com.govscheme.scheme.entity.SchemeState;
import com.govscheme.scheme.entity.SchemeStateRepository;
import com.govscheme.scheme.entity.SchemeTag;
import com.govscheme.scheme.entity.SchemeTagRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Persists one scheme (upsert by slug): normalized row, verbatim raw payload
 * and replaced child collections. Normalization is best-effort — fields the
 * source omits stay {@code null} and are never invented.
 */
@Service
public class SchemeUpsertService {

    private final SchemeRepository schemeRepository;
    private final SchemeRawDataRepository rawDataRepository;
    private final SchemeTagRepository tagRepository;
    private final SchemeStateRepository stateRepository;
    private final SchemeFaqRepository faqRepository;
    private final SchemeDocumentRepository documentRepository;
    private final SchemeApplicationStepRepository stepRepository;
    private final SchemeCategoryRepository categoryRepository;
    private final SchemeBeneficiaryRepository beneficiaryRepository;
    private final SchemeReferenceRepository referenceRepository;
    private final ObjectMapper objectMapper;

    public SchemeUpsertService(SchemeRepository schemeRepository,
                               SchemeRawDataRepository rawDataRepository,
                               SchemeTagRepository tagRepository,
                               SchemeStateRepository stateRepository,
                               SchemeFaqRepository faqRepository,
                               SchemeDocumentRepository documentRepository,
                               SchemeApplicationStepRepository stepRepository,
                               SchemeCategoryRepository categoryRepository,
                               SchemeBeneficiaryRepository beneficiaryRepository,
                               SchemeReferenceRepository referenceRepository,
                               ObjectMapper objectMapper) {
        this.schemeRepository = schemeRepository;
        this.rawDataRepository = rawDataRepository;
        this.tagRepository = tagRepository;
        this.stateRepository = stateRepository;
        this.faqRepository = faqRepository;
        this.documentRepository = documentRepository;
        this.stepRepository = stepRepository;
        this.categoryRepository = categoryRepository;
        this.beneficiaryRepository = beneficiaryRepository;
        this.referenceRepository = referenceRepository;
        this.objectMapper = objectMapper;
    }

    public enum Outcome {
        CREATED, UPDATED
    }

    @Transactional
    public Outcome upsert(String slug, String detailJson, String faqsJson,
                          String documentsJson, String lang) throws Exception {
        JsonNode detail = unwrap(objectMapper.readTree(detailJson));
        boolean created = false;

        Scheme scheme = schemeRepository.findBySlug(slug).orElseGet(() -> {
            Scheme fresh = new Scheme();
            fresh.setSlug(slug);
            return fresh;
        });
        if (scheme.getId() == null) {
            created = true;
        }

        scheme.setSchemeName(text(detail, "schemeName"));
        scheme.setSchemeNameEng(text(detail, "schemeNameEng"));
        scheme.setShortTitle(text(detail, "schemeShortTitle"));
        scheme.setSchemeCategory(text(detail, "schemeCategory"));
        scheme.setBeneficiaryState(text(detail, "beneficiaryState"));
        scheme.setLevel(text(detail, "level"));
        scheme.setSchemeFor(text(detail, "schemeFor"));
        scheme.setNodalMinistry(text(detail, "nodalMinistryName"));
        scheme.setBriefDescription(text(detail, "briefDescription"));
        scheme.setBriefDescriptionEng(text(detail, "briefDescriptionEng"));
        scheme.setCloseDate(text(detail, "schemeCloseDate"));
        scheme.setPriority(intOrNull(detail, "priority"));
        scheme.setSource("MYSCHEME");
        scheme.setSourceUrl("https://www.myscheme.gov.in/schemes/" + slug);
        scheme.setLastSyncedAt(Instant.now());
        scheme = schemeRepository.save(scheme);

        SchemeRawData raw = rawDataRepository.findBySlug(slug).orElseGet(SchemeRawData::new);
        raw.setSchemeId(scheme.getId());
        raw.setSlug(slug);
        raw.setKind("DETAIL");
        raw.setPayload(detailJson);
        rawDataRepository.save(raw);

        replaceTags(scheme, detail, lang);
        replaceStates(scheme, detail);
        replaceFaqs(scheme, faqsJson, lang);
        replaceDocuments(scheme, documentsJson, lang);
        replaceSteps(scheme, detail, lang);

        return created ? Outcome.CREATED : Outcome.UPDATED;
    }

    /**
     * Upserts one item from an offline scheme dump (data/schemes.json shape).
     * Same idempotency and never-invent guarantees as {@link #upsert}: the
     * whole item is preserved verbatim in {@code scheme_raw_data} (kind FILE).
     */
    @Transactional
    public Outcome upsertFromFile(JsonNode item) {
        String slug = text(item, "slug");
        if (slug == null || slug.isBlank()) {
            throw new IllegalArgumentException("File item without slug cannot be imported");
        }
        boolean created = false;
        Scheme scheme = schemeRepository.findBySlug(slug).orElseGet(() -> {
            Scheme fresh = new Scheme();
            fresh.setSlug(slug);
            return fresh;
        });
        if (scheme.getId() == null) {
            created = true;
        }

        scheme.setSchemeName(text(item, "schemeName"));
        scheme.setShortTitle(text(item, "shortTitle"));
        scheme.setSchemeType(text(item, "schemeType"));
        scheme.setNodalMinistry(text(item, "ministry"));
        scheme.setDepartment(text(item, "department"));
        scheme.setLevel(text(item, "level"));
        scheme.setSchemeFor(text(item, "schemeFor"));
        scheme.setBenefitType(text(item, "benefitType"));
        scheme.setOpenDate(text(item, "openDate"));
        scheme.setImageUrl(text(item, "imageUrl"));
        scheme.setExternalId(text(item, "id"));
        scheme.setBriefDescription(text(item, "briefDescription"));
        scheme.setDetailedDescriptionMd(text(item, "detailedDescription_md"));
        scheme.setBenefitsMd(text(item, "benefits_md"));
        scheme.setExclusionsMd(text(item, "exclusions_md"));
        scheme.setEligibilityMd(text(item, "eligibility_md"));
        scheme.setDocumentsMd(text(item, "documents_md"));
        scheme.setSource("FILE");
        scheme.setSourceUrl(text(item, "sourceUrl"));
        scheme.setLastSyncedAt(Instant.now());
        scheme = schemeRepository.save(scheme);

        SchemeRawData raw = rawDataRepository.findBySlug(slug).orElseGet(SchemeRawData::new);
        raw.setSchemeId(scheme.getId());
        raw.setSlug(slug);
        raw.setKind("FILE");
        raw.setPayload(item.toString());
        rawDataRepository.save(raw);

        replaceFileTags(scheme, item);
        replaceFileCategories(scheme, item);
        replaceFileBeneficiaries(scheme, item);
        replaceFileStates(scheme, item);
        replaceFileFaqs(scheme, item);
        replaceFileDocuments(scheme, item);
        replaceFileReferences(scheme, item);
        replaceFileSteps(scheme, item);

        return created ? Outcome.CREATED : Outcome.UPDATED;
    }

    private void replaceFileTags(Scheme scheme, JsonNode item) {
        tagRepository.deleteBySchemeId(scheme.getId());
        addTags(scheme, item.get("tags"), "en");
    }

    private void replaceFileCategories(Scheme scheme, JsonNode item) {
        categoryRepository.deleteBySchemeId(scheme.getId());
        addCategories(scheme, item.get("categories"), SchemeCategory.KIND_MAIN);
        addCategories(scheme, item.get("subCategories"), SchemeCategory.KIND_SUB);
    }

    private void addCategories(Scheme scheme, JsonNode list, String kind) {
        if (list == null || !list.isArray()) {
            return;
        }
        for (JsonNode node : list) {
            String value = node.isTextual() ? node.asText() : null;
            if (value == null || value.isBlank()) {
                continue;
            }
            SchemeCategory row = new SchemeCategory();
            row.setSchemeId(scheme.getId());
            row.setCategory(value.trim());
            row.setKind(kind);
            categoryRepository.save(row);
        }
    }

    private void replaceFileBeneficiaries(Scheme scheme, JsonNode item) {
        beneficiaryRepository.deleteBySchemeId(scheme.getId());
        JsonNode list = item.get("beneficiaries");
        if (list == null || !list.isArray()) {
            return;
        }
        for (JsonNode node : list) {
            String value = node.isTextual() ? node.asText() : null;
            if (value == null || value.isBlank()) {
                continue;
            }
            SchemeBeneficiary row = new SchemeBeneficiary();
            row.setSchemeId(scheme.getId());
            row.setBeneficiary(value.trim());
            beneficiaryRepository.save(row);
        }
    }

    private void replaceFileStates(Scheme scheme, JsonNode item) {
        // Dumps carry no per-state field; keep any states resolved from the
        // API path untouched by not deleting here. File rows simply add none.
    }

    private void replaceFileFaqs(Scheme scheme, JsonNode item) {
        faqRepository.deleteBySchemeId(scheme.getId());
        JsonNode list = item.get("faqs");
        if (list == null || !list.isArray()) {
            return;
        }
        int pos = 0;
        for (JsonNode faq : list) {
            String question = firstText(faq, "question", "q", "title");
            if (question == null || question.isBlank()) {
                continue;
            }
            SchemeFaq row = new SchemeFaq();
            row.setSchemeId(scheme.getId());
            row.setQuestion(question.trim());
            row.setAnswer(firstText(faq, "answer", "answer_md", "a", "description", "description_md"));
            row.setLanguage("en");
            row.setPosition(pos++);
            faqRepository.save(row);
        }
    }

    private void replaceFileDocuments(Scheme scheme, JsonNode item) {
        documentRepository.deleteBySchemeId(scheme.getId());
        JsonNode raw = item.get("documents_raw");
        if (raw == null || !raw.isArray()) {
            return;
        }
        int pos = 0;
        for (JsonNode block : raw) {
            for (String line : slateParagraphTexts(block)) {
                String name = cleanDocumentLine(line);
                if (name == null) {
                    continue;
                }
                SchemeDocument row = new SchemeDocument();
                row.setSchemeId(scheme.getId());
                row.setName(name);
                row.setRequired(true);
                row.setLanguage("en");
                row.setPosition(pos++);
                documentRepository.save(row);
            }
        }
    }

    /**
     * Flattens one Slate paragraph block into its text lines. Headings
     * (lines ending with ':') and over-long prose are skipped — they are
     * display text, not document names; the full markdown stays queryable
     * in {@code documents_md} and the raw payload.
     */
    private List<String> slateParagraphTexts(JsonNode block) {
        List<String> lines = new ArrayList<>();
        if (block == null || !block.isObject()) {
            return lines;
        }
        if (!"paragraph".equals(text(block, "type"))) {
            return lines;
        }
        JsonNode children = block.get("children");
        if (children == null || !children.isArray()) {
            return lines;
        }
        StringBuilder current = new StringBuilder();
        for (JsonNode child : children) {
            String text = text(child, "text");
            if (text != null) {
                current.append(text);
            }
        }
        for (String line : current.toString().split("\\r?\\n")) {
            String trimmed = line.trim();
            if (!trimmed.isEmpty()) {
                lines.add(trimmed);
            }
        }
        return lines;
    }

    private String cleanDocumentLine(String line) {
        String cleaned = line.replaceAll("^[\\s\\-•*·]+", "")
            .replaceAll("^[a-zA-Z0-9]+[.\\)]\\s+", "")
            .trim();
        if (cleaned.isEmpty() || cleaned.endsWith(":") || cleaned.length() > 200) {
            return null;
        }
        return cleaned;
    }

    private void replaceFileReferences(Scheme scheme, JsonNode item) {
        referenceRepository.deleteBySchemeId(scheme.getId());
        JsonNode list = item.get("references");
        if (list == null || !list.isArray()) {
            return;
        }
        int pos = 0;
        for (JsonNode ref : list) {
            String title = firstText(ref, "title", "name");
            String url = firstText(ref, "url", "link");
            if (title == null || title.isBlank() || url == null || url.isBlank()) {
                continue;
            }
            SchemeReference row = new SchemeReference();
            row.setSchemeId(scheme.getId());
            row.setTitle(title.trim());
            row.setUrl(url.trim());
            row.setPosition(pos++);
            referenceRepository.save(row);
        }
    }

    private void replaceFileSteps(Scheme scheme, JsonNode item) {
        stepRepository.deleteBySchemeId(scheme.getId());
        JsonNode list = item.get("applicationProcess");
        if (list == null || !list.isArray()) {
            return;
        }
        int stepNo = 1;
        for (JsonNode channel : list) {
            String mode = firstText(channel, "mode");
            String prefix = mode != null && !mode.isBlank() ? "[" + mode.trim() + "] " : "";
            String processMd = firstText(channel, "process_md", "process", "description");
            if (processMd == null || processMd.isBlank()) {
                continue;
            }
            for (String line : processMd.split("\\r?\\n")) {
                String cleaned = line.replaceAll("^[\\d.\\)\\-\\s*]+", "")
                    .replaceAll("^\\*\\*Step \\d+:\\*\\*\\s*", "")
                    .replaceAll("^Step \\d+:\\s*", "")
                    .trim();
                if (cleaned.isEmpty()) {
                    continue;
                }
                SchemeApplicationStep step = new SchemeApplicationStep();
                step.setSchemeId(scheme.getId());
                step.setStepNo(stepNo++);
                step.setDescription(prefix + cleaned);
                step.setLanguage("en");
                stepRepository.save(step);
            }
        }
    }

    private void replaceTags(Scheme scheme, JsonNode detail, String lang) {
        tagRepository.deleteBySchemeId(scheme.getId());
        addTags(scheme, array(detail, "tags"), lang);
        addTags(scheme, array(detail, "tagsEng"), "en");
    }

    private void addTags(Scheme scheme, JsonNode tags, String language) {
        if (tags == null || !tags.isArray()) {
            return;
        }
        for (JsonNode tag : tags) {
            String value = tag.isTextual() ? tag.asText() : tag.toString();
            if (value == null || value.isBlank()) {
                continue;
            }
            SchemeTag row = new SchemeTag();
            row.setSchemeId(scheme.getId());
            row.setTag(value.trim());
            row.setLanguage(language);
            tagRepository.save(row);
        }
    }

    private void replaceStates(Scheme scheme, JsonNode detail) {
        stateRepository.deleteBySchemeId(scheme.getId());
        boolean any = false;
        for (String key : new String[]{"states", "beneficiaryStates", "eligibleStates"}) {
            JsonNode arr = detail.get(key);
            if (arr != null && arr.isArray()) {
                for (JsonNode s : arr) {
                    String state = s.isTextual() ? s.asText() : text(s, "name");
                    if (state != null && !state.isBlank()) {
                        SchemeState row = new SchemeState();
                        row.setSchemeId(scheme.getId());
                        row.setState(state.trim());
                        stateRepository.save(row);
                        any = true;
                    }
                }
            }
        }
        if (!any) {
            String single = text(detail, "beneficiaryState");
            if (single != null && !single.isBlank()) {
                SchemeState row = new SchemeState();
                row.setSchemeId(scheme.getId());
                row.setState(single.trim());
                stateRepository.save(row);
            }
        }
    }

    private void replaceFaqs(Scheme scheme, String faqsJson, String lang) throws Exception {
        faqRepository.deleteBySchemeId(scheme.getId());
        if (faqsJson == null || faqsJson.isBlank()) {
            return;
        }
        JsonNode root = unwrap(objectMapper.readTree(faqsJson));
        JsonNode list = array(root, "faqs");
        if (list == null) {
            list = array(root, "faq");
        }
        if (list == null || !list.isArray()) {
            return;
        }
        int pos = 0;
        for (JsonNode item : list) {
            String question = firstText(item, "question", "q", "title");
            if (question == null || question.isBlank()) {
                continue;
            }
            SchemeFaq faq = new SchemeFaq();
            faq.setSchemeId(scheme.getId());
            faq.setQuestion(question.trim());
            faq.setAnswer(firstText(item, "answer", "a", "description"));
            faq.setLanguage(lang);
            faq.setPosition(pos++);
            faqRepository.save(faq);
        }
    }

    private void replaceDocuments(Scheme scheme, String documentsJson, String lang) throws Exception {
        documentRepository.deleteBySchemeId(scheme.getId());
        if (documentsJson == null || documentsJson.isBlank()) {
            return;
        }
        JsonNode root = unwrap(objectMapper.readTree(documentsJson));
        JsonNode list = array(root, "documents");
        if (list == null) {
            list = array(root, "requiredDocuments");
        }
        if (list == null || !list.isArray()) {
            return;
        }
        int pos = 0;
        for (JsonNode item : list) {
            String name;
            boolean required = true;
            if (item.isTextual()) {
                name = item.asText();
            } else {
                name = firstText(item, "name", "documentName", "title");
                JsonNode req = item.get("required");
                if (req == null) {
                    req = item.get("isRequired");
                }
                if (req != null && req.isBoolean()) {
                    required = req.asBoolean();
                }
            }
            if (name == null || name.isBlank()) {
                continue;
            }
            SchemeDocument doc = new SchemeDocument();
            doc.setSchemeId(scheme.getId());
            doc.setName(name.trim());
            doc.setRequired(required);
            doc.setLanguage(lang);
            doc.setPosition(pos++);
            documentRepository.save(doc);
        }
    }

    private void replaceSteps(Scheme scheme, JsonNode detail, String lang) {
        stepRepository.deleteBySchemeId(scheme.getId());
        JsonNode node = null;
        for (String key : new String[]{"applicationProcess", "process", "steps", "howToApply"}) {
            JsonNode candidate = detail.get(key);
            if (candidate != null && !candidate.isNull()) {
                node = candidate;
                break;
            }
        }
        if (node == null || node.isNull()) {
            return;
        }
        int stepNo = 1;
        if (node.isArray()) {
            for (JsonNode item : node) {
                String text = item.isTextual() ? item.asText()
                    : firstText(item, "description", "step", "text", "title");
                if (text == null || text.isBlank()) {
                    continue;
                }
                saveStep(scheme, stepNo++, text.trim(), lang);
            }
        } else if (node.isTextual()) {
            for (String line : node.asText().split("\\r?\\n")) {
                String cleaned = line.replaceAll("^[\\d.\\)\\-\\s]+", "").trim();
                if (!cleaned.isEmpty()) {
                    saveStep(scheme, stepNo++, cleaned, lang);
                }
            }
        }
    }

    private void saveStep(Scheme scheme, int stepNo, String description, String lang) {
        SchemeApplicationStep step = new SchemeApplicationStep();
        step.setSchemeId(scheme.getId());
        step.setStepNo(stepNo);
        step.setDescription(description);
        step.setLanguage(lang);
        stepRepository.save(step);
    }

    private JsonNode unwrap(JsonNode root) {
        if (root != null && root.isObject() && root.has("data")
                && root.get("data").isObject() && root.size() == 1) {
            return root.get("data");
        }
        return root;
    }

    private JsonNode array(JsonNode node, String key) {
        if (node == null || !node.isObject()) {
            return null;
        }
        JsonNode value = node.get(key);
        if (value != null && value.isArray()) {
            return value;
        }
        if (node.has("data") && node.get("data").isObject()) {
            JsonNode inner = node.get("data").get(key);
            if (inner != null && inner.isArray()) {
                return inner;
            }
        }
        return null;
    }

    private String text(JsonNode node, String key) {
        if (node == null || !node.isObject()) {
            return null;
        }
        JsonNode value = node.get(key);
        if (value == null || value.isNull()) {
            if (node.has("data") && node.get("data").isObject()) {
                value = node.get("data").get(key);
            }
        }
        if (value == null || value.isNull() || !value.isValueNode()) {
            return null;
        }
        String text = value.asText();
        return text.isBlank() ? null : text;
    }

    private String firstText(JsonNode node, String... keys) {
        for (String key : keys) {
            String value = text(node, key);
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    private Integer intOrNull(JsonNode node, String key) {
        if (node == null || !node.isObject()) {
            return null;
        }
        JsonNode value = node.get(key);
        if (value == null || value.isNull()) {
            return null;
        }
        if (value.isNumber()) {
            return value.asInt();
        }
        try {
            return Integer.parseInt(value.asText().trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
