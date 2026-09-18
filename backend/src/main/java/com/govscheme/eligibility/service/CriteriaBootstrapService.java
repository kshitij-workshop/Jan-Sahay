package com.govscheme.eligibility.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.govscheme.eligibility.entity.SchemeEligibility;
import com.govscheme.eligibility.entity.SchemeEligibilityRepository;
import com.govscheme.scheme.entity.Scheme;
import com.govscheme.scheme.entity.SchemeRawData;
import com.govscheme.scheme.entity.SchemeRawDataRepository;
import com.govscheme.scheme.entity.SchemeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.regex.Pattern;

/**
 * Derives machine-readable criteria from unambiguous source signals only:
 * deterministic text mining ({@link CriteriaExtractor}) over published
 * eligibility text, plus an explicit-naming fallback ({@code states=Bihar}
 * when the scheme's own name/ministry/department says so). Anything uncertain
 * is left uncurated — evaluating to INSUFFICIENT_INFORMATION — because
 * inventing requirements is worse than admitting ignorance. Existing criteria
 * rows are never overwritten.
 */
@Service
public class CriteriaBootstrapService {

    private static final Logger log = LoggerFactory.getLogger(CriteriaBootstrapService.class);
    private static final Pattern BIHAR = Pattern.compile("\\bbihar\\b|बिहार", Pattern.CASE_INSENSITIVE);

    private final SchemeRepository schemeRepository;
    private final SchemeEligibilityRepository eligibilityRepository;
    private final SchemeRawDataRepository rawDataRepository;
    private final ObjectMapper objectMapper;

    public CriteriaBootstrapService(SchemeRepository schemeRepository,
                                    SchemeEligibilityRepository eligibilityRepository,
                                    SchemeRawDataRepository rawDataRepository,
                                    ObjectMapper objectMapper) {
        this.schemeRepository = schemeRepository;
        this.eligibilityRepository = eligibilityRepository;
        this.rawDataRepository = rawDataRepository;
        this.objectMapper = objectMapper;
    }

    public record BootstrapSummary(int scanned, int curated, int alreadyCurated, int skipped) {
    }

    @Transactional
    public BootstrapSummary bootstrap() {
        List<Scheme> schemes = schemeRepository.findAll();
        int curated = 0;
        int alreadyCurated = 0;
        for (Scheme scheme : schemes) {
            if (eligibilityRepository.existsById(scheme.getId())) {
                alreadyCurated++;
                continue;
            }
            SchemeEligibility criteria = extractFor(scheme);
            if (isEmpty(criteria)) {
                continue;
            }
            criteria.setSchemeId(scheme.getId());
            eligibilityRepository.save(criteria);
            curated++;
        }
        BootstrapSummary summary =
            new BootstrapSummary(schemes.size(), curated, alreadyCurated, schemes.size() - curated - alreadyCurated);
        log.info("CURATION_BOOTSTRAPPED scanned={} curated={} alreadyCurated={} skipped={}",
            summary.scanned(), summary.curated(), summary.alreadyCurated(), summary.skipped());
        return summary;
    }

    private SchemeEligibility extractFor(Scheme scheme) {
        SchemeEligibility criteria = new SchemeEligibility();
        rawDataRepository.findBySlug(scheme.getSlug()).ifPresent(raw -> {
            try {
                SchemeEligibility mined = CriteriaExtractor.extract(objectMapper.readTree(raw.getPayload()));
                merge(mined, criteria);
            } catch (Exception e) {
                log.warn("CURATION_EXTRACT_FAILED slug={} message={}", scheme.getSlug(), e.getMessage());
            }
        });
        if (criteria.getStates() == null && mentionsBihar(scheme)) {
            criteria.setStates("Bihar");
        }
        return criteria;
    }

    private void merge(SchemeEligibility from, SchemeEligibility into) {
        if (from.getMinAge() != null) {
            into.setMinAge(from.getMinAge());
        }
        if (from.getMaxAge() != null) {
            into.setMaxAge(from.getMaxAge());
        }
        if (from.getStates() != null) {
            into.setStates(from.getStates());
        }
        if (from.getGenders() != null) {
            into.setGenders(from.getGenders());
        }
        if (from.getMaxAnnualIncome() != null) {
            into.setMaxAnnualIncome(from.getMaxAnnualIncome());
        }
        if (from.getCasteCategories() != null) {
            into.setCasteCategories(from.getCasteCategories());
        }
        if (from.getOccupations() != null) {
            into.setOccupations(from.getOccupations());
        }
        if (from.getRequireStudent() != null) {
            into.setRequireStudent(from.getRequireStudent());
        }
        if (from.getRequireDisabled() != null) {
            into.setRequireDisabled(from.getRequireDisabled());
        }
        if (from.getMinDisabilityPercentage() != null) {
            into.setMinDisabilityPercentage(from.getMinDisabilityPercentage());
        }
        if (from.getAreaTypes() != null) {
            into.setAreaTypes(from.getAreaTypes());
        }
        if (from.getEmploymentStatuses() != null) {
            into.setEmploymentStatuses(from.getEmploymentStatuses());
        }
        if (from.getRequireGovtEmployee() != null) {
            into.setRequireGovtEmployee(from.getRequireGovtEmployee());
        }
        if (from.getRequireBpl() != null) {
            into.setRequireBpl(from.getRequireBpl());
        }
        if (from.getMaritalStatuses() != null) {
            into.setMaritalStatuses(from.getMaritalStatuses());
        }
        if (from.getRequireMinority() != null) {
            into.setRequireMinority(from.getRequireMinority());
        }
    }

    private boolean isEmpty(SchemeEligibility criteria) {
        return criteria.getMinAge() == null && criteria.getMaxAge() == null
            && criteria.getStates() == null && criteria.getGenders() == null
            && criteria.getMaxAnnualIncome() == null && criteria.getCasteCategories() == null
            && criteria.getOccupations() == null && criteria.getRequireStudent() == null
            && criteria.getRequireDisabled() == null && criteria.getMinDisabilityPercentage() == null
            && criteria.getAreaTypes() == null && criteria.getEmploymentStatuses() == null
            && criteria.getRequireGovtEmployee() == null && criteria.getRequireBpl() == null
            && criteria.getMaritalStatuses() == null && criteria.getRequireMinority() == null;
    }

    private boolean mentionsBihar(Scheme scheme) {
        return matches(scheme.getSchemeName())
            || matches(scheme.getNodalMinistry())
            || matches(scheme.getDepartment());
    }

    private boolean matches(String value) {
        return value != null && BIHAR.matcher(value).find();
    }
}
