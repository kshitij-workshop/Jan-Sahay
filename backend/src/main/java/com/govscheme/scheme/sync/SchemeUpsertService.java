package com.govscheme.scheme.sync;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.govscheme.scheme.entity.Scheme;
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
    private final ObjectMapper objectMapper;

    public SchemeUpsertService(SchemeRepository schemeRepository,
                               SchemeRawDataRepository rawDataRepository,
                               SchemeTagRepository tagRepository,
                               SchemeStateRepository stateRepository,
                               SchemeFaqRepository faqRepository,
                               SchemeDocumentRepository documentRepository,
                               SchemeApplicationStepRepository stepRepository,
                               ObjectMapper objectMapper) {
        this.schemeRepository = schemeRepository;
        this.rawDataRepository = rawDataRepository;
        this.tagRepository = tagRepository;
        this.stateRepository = stateRepository;
        this.faqRepository = faqRepository;
        this.documentRepository = documentRepository;
        this.stepRepository = stepRepository;
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
