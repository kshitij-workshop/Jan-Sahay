package com.govscheme.scheme.service;

import com.govscheme.common.dto.PageResponse;
import com.govscheme.common.exception.ResourceNotFoundException;
import com.govscheme.scheme.dto.SchemeDetailResponse;
import com.govscheme.scheme.dto.SchemeSummaryResponse;
import com.govscheme.scheme.entity.Scheme;
import com.govscheme.scheme.entity.SchemeApplicationStepRepository;
import com.govscheme.scheme.entity.SchemeBeneficiaryRepository;
import com.govscheme.scheme.entity.SchemeCategoryRepository;
import com.govscheme.scheme.entity.SchemeDocumentRepository;
import com.govscheme.scheme.entity.SchemeFaqRepository;
import com.govscheme.scheme.entity.SchemeReferenceRepository;
import com.govscheme.scheme.entity.SchemeRepository;
import com.govscheme.scheme.entity.SchemeStateRepository;
import com.govscheme.scheme.entity.SchemeTagRepository;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Read-only scheme browsing over the synced catalog. Text search matches
 * name/short-title/descriptions/category; category and state filters also
 * consult their child tables so file-imported rows (which keep those only
 * as children) are found. Demographic filters (age, income, caste, …) belong
 * to the eligibility engine in Phase 6, not to browsing.
 */
@Service
public class SchemeQueryService {

    private final SchemeRepository schemeRepository;
    private final SchemeTagRepository tagRepository;
    private final SchemeStateRepository stateRepository;
    private final SchemeCategoryRepository categoryRepository;
    private final SchemeBeneficiaryRepository beneficiaryRepository;
    private final SchemeFaqRepository faqRepository;
    private final SchemeDocumentRepository documentRepository;
    private final SchemeApplicationStepRepository stepRepository;
    private final SchemeReferenceRepository referenceRepository;

    public SchemeQueryService(SchemeRepository schemeRepository,
                              SchemeTagRepository tagRepository,
                              SchemeStateRepository stateRepository,
                              SchemeCategoryRepository categoryRepository,
                              SchemeBeneficiaryRepository beneficiaryRepository,
                              SchemeFaqRepository faqRepository,
                              SchemeDocumentRepository documentRepository,
                              SchemeApplicationStepRepository stepRepository,
                              SchemeReferenceRepository referenceRepository) {
        this.schemeRepository = schemeRepository;
        this.tagRepository = tagRepository;
        this.stateRepository = stateRepository;
        this.categoryRepository = categoryRepository;
        this.beneficiaryRepository = beneficiaryRepository;
        this.faqRepository = faqRepository;
        this.documentRepository = documentRepository;
        this.stepRepository = stepRepository;
        this.referenceRepository = referenceRepository;
    }

    @Transactional(readOnly = true)
    public PageResponse<SchemeSummaryResponse> search(String query, String category,
                                                      String state, String level,
                                                      int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(0, page), Math.min(100, Math.max(1, size)),
            Sort.by(Sort.Direction.ASC, "schemeName"));
        Specification<Scheme> spec = (root, q, cb) -> cb.conjunction();
        if (query != null && !query.isBlank()) {
            String like = "%" + query.trim().toLowerCase() + "%";
            spec = spec.and((root, q, cb) -> cb.or(
                cb.like(cb.lower(root.get("schemeName")), like),
                cb.like(cb.lower(root.get("schemeNameEng")), like),
                cb.like(cb.lower(root.get("shortTitle")), like),
                cb.like(cb.lower(root.get("schemeCategory")), like),
                cb.like(cb.lower(root.get("briefDescription")), like),
                cb.like(cb.lower(root.get("briefDescriptionEng")), like)));
        }
        if (category != null && !category.isBlank()) {
            String wanted = category.trim();
            spec = spec.and((root, q, cb) -> {
                Subquery<String> sub = q.subquery(String.class);
                var child = sub.from(com.govscheme.scheme.entity.SchemeCategory.class);
                sub.select(child.get("schemeId"))
                    .where(cb.and(
                        cb.equal(child.get("schemeId"), root.get("id")),
                        cb.equal(child.get("category"), wanted)));
                return cb.or(cb.equal(root.get("schemeCategory"), wanted), cb.exists(sub));
            });
        }
        if (state != null && !state.isBlank()) {
            String wanted = state.trim();
            spec = spec.and((root, q, cb) -> {
                Subquery<String> sub = q.subquery(String.class);
                var child = sub.from(com.govscheme.scheme.entity.SchemeState.class);
                sub.select(child.get("schemeId"))
                    .where(cb.and(
                        cb.equal(child.get("schemeId"), root.get("id")),
                        cb.equal(child.get("state"), wanted)));
                return cb.or(
                    cb.equal(root.get("beneficiaryState"), wanted),
                    cb.equal(root.get("beneficiaryState"), "All India"),
                    cb.exists(sub));
            });
        }
        if (level != null && !level.isBlank()) {
            String wanted = level.trim();
            spec = spec.and((root, q, cb) -> cb.equal(root.get("level"), wanted));
        }
        Page<Scheme> result = schemeRepository.findAll(spec, pageable);
        return PageResponse.from(result, SchemeSummaryResponse::from);
    }

    @Transactional(readOnly = true)
    public SchemeDetailResponse getById(String id) {
        Scheme scheme = schemeRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Scheme", "id", id));
        return SchemeDetailResponse.from(
            scheme,
            tagRepository.findBySchemeId(id),
            stateRepository.findBySchemeId(id),
            categoryRepository.findBySchemeId(id),
            beneficiaryRepository.findBySchemeId(id),
            faqRepository.findBySchemeIdOrderByPositionAsc(id),
            documentRepository.findBySchemeIdOrderByPositionAsc(id),
            stepRepository.findBySchemeIdOrderByStepNoAsc(id),
            referenceRepository.findBySchemeIdOrderByPositionAsc(id));
    }
}
